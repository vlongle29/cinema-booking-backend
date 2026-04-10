package com.example.CineBook.service;

import com.example.CineBook.dto.seat.SeatResponse;
import com.example.CineBook.dto.seattemplate.*;

import java.util.List;
import java.util.UUID;

public interface SeatTemplateService {

    SeatTemplateResponse createTemplate(CreateSeatTemplateRequest request);

    SeatTemplateResponse addSeatsToTemplate(UUID templateId, AddSeatsToTemplateRequest request);

    List<SeatTemplateResponse> getAllTemplates();

    SeatTemplateResponse getTemplateById(UUID templateId);

    void deleteTemplate(UUID templateId);

    List<SeatResponse> applyTemplateToRoom(UUID roomId, UUID templateId);
}
