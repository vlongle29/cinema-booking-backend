package com.example.CineBook.service.impl;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.city.CityResponse;
import com.example.CineBook.dto.city.CitySearchDTO;
import com.example.CineBook.dto.city.CreateCityRequest;
import com.example.CineBook.dto.city.UpdateCityRequest;
import com.example.CineBook.mapper.CityMapper;
import com.example.CineBook.model.City;
import com.example.CineBook.repository.irepository.CityRepository;
import com.example.CineBook.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CITY_CACHE_KEY = "cityCache:";
    private static final String ALL_CITIES_KEY = "cities:all";
    private static final String SEARCH_CACHE_KEY = "cities:search:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @Override
    @Transactional
    public CityResponse createCity(CreateCityRequest request) {
        if (cityRepository.existsByName(request.getName())) {
            throw new BusinessException(MessageCode.CITY_NAME_ALREADY_EXISTS);
        }

        City city = cityMapper.toEntity(request);
        City saved = cityRepository.save(city);
        
        redisTemplate.delete(ALL_CITIES_KEY);
        
        return cityMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> getAllCities() {
        @SuppressWarnings("unchecked")
        List<CityResponse> cached = (List<CityResponse>) redisTemplate.opsForValue().get(ALL_CITIES_KEY);
        
        if (cached != null) {
            return cached;
        }
        
        List<CityResponse> cities = cityRepository.findAll().stream()
                .filter(city -> !Boolean.TRUE.equals(city.getIsDelete()))
                .map(cityMapper::toResponse)
                .collect(Collectors.toList());
        
        if (!cities.isEmpty()) {
            redisTemplate.opsForValue().set(ALL_CITIES_KEY, cities, CACHE_TTL);
        }
        
        return cities;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CityResponse> searchCities(CitySearchDTO searchDTO) {
        String cacheKey = SEARCH_CACHE_KEY + searchDTO.hashCode();
        @SuppressWarnings("unchecked")
        PageResponse<CityResponse> cached = (PageResponse<CityResponse>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return cached;
        }
        
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<City> entityPage = cityRepository.searchWithFilters(searchDTO, pageable);
        Page<CityResponse> responsePage = entityPage.map(cityMapper::toResponse);
        PageResponse<CityResponse> result = PageResponse.of(responsePage);
        
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL);
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CityResponse getCityById(UUID id) {
        String cacheKey = CITY_CACHE_KEY + id;
        CityResponse cached = (CityResponse) redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return cached;
        }
        
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.CITY_NOT_FOUND));
        
        if (Boolean.TRUE.equals(city.getIsDelete())) {
            throw new BusinessException(MessageCode.CITY_NOT_FOUND);
        }
        
        CityResponse response = cityMapper.toResponse(city);
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
        
        return response;
    }

    @Override
    @Transactional
    public CityResponse updateCity(UUID id, UpdateCityRequest request) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.CITY_NOT_FOUND));

        if (request.getName() != null && !request.getName().equals(city.getName())) {
            if (cityRepository.existsByName(request.getName())) {
                throw new BusinessException(MessageCode.CITY_NAME_ALREADY_EXISTS);
            }
        }

        cityMapper.updateEntityFromDto(request, city);
        City updated = cityRepository.save(city);
        
        redisTemplate.delete(CITY_CACHE_KEY + id);
        redisTemplate.delete(ALL_CITIES_KEY);
        clearSearchCache();
        
        return cityMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCity(UUID id) {
        if (!cityRepository.existsById(id)) {
            throw new BusinessException(MessageCode.CITY_NOT_FOUND);
        }

        cityRepository.softDeleteById(id);
        
        redisTemplate.delete(CITY_CACHE_KEY + id);
        redisTemplate.delete(ALL_CITIES_KEY);
        clearSearchCache();
    }

    private void clearSearchCache() {
        redisTemplate.keys(SEARCH_CACHE_KEY + "*").forEach(redisTemplate::delete);
    }
}
