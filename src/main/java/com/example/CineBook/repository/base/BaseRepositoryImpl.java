package com.example.CineBook.repository.base;

import com.example.CineBook.common.constant.DelFlag;
import com.example.CineBook.common.dto.request.SearchBaseDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BaseRepositoryImpl<T, S extends SearchBaseDto> implements  BaseRepositoryCustom<T, S> {

    @PersistenceContext
    protected EntityManager entityManager;

    private final Class<T> domainClass;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    public BaseRepositoryImpl(Class<T> domainClass) {
        this.domainClass = domainClass;
    }

    /**
     * Xây dựng danh sách các điều kiện lọc (Predicate) dựa trên DTO tìm kiếm.
     * Các lớp con có thể ghi đè (override) phương thức này để cung cấp logic lọc tùy chỉnh.
     * <p>
     * Cài đặt mặc định trả về một danh sách rỗng, nghĩa là không có bộ lọc nào được áp dụng.
     *
     * @param root      Đối tượng Root của Criteria API.
     * @param query     Đối tượng CriteriaQuery của truy vấn hiện tại. Cần thiết để tạo các subquery.
     * @param cb        Đối tượng CriteriaBuilder của Criteria API.
     * @param searchDTO DTO chứa các tham số tìm kiếm.
     * @return Danh sách các Predicate sẽ được áp dụng cho mệnh đề WHERE.
     */
    protected List<Predicate> buildPredicates(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb, S searchDTO) {
        // Cung cấp cài đặt mặc định: không có điều kiện lọc nào.
        return new ArrayList<>();
    }

    /**
     * Tìm tất cả các entity với bộ lọc và phân trang.
     * Phương thức này sử dụng entity class làm kết quả trả về.
     *
     * @param searchDTO DTO chứa các điều kiện tìm kiếm, phân trang và sắp xếp
     * @return Trang chứa các entity khớp điều kiện
     */
    @Override
    public Page<T> findAllWithFilters(S searchDTO) {
        return this.findAllWithFilters(searchDTO, this.domainClass);
    }

    /**
     * Tìm tất cả các entity với bộ lọc và phân trang, trả về dạng projection.
     * Phương thức này cho phép ánh xạ kết quả sang một lớp DTO khác để tối ưu hiệu năng.
     *
     * @param searchDTO       DTO chứa các điều kiện tìm kiếm, phân trang và sắp xếp
     * @param projectionClass Lớp DTO để ánh xạ kết quả
     * @return Trang chứa các DTO projection khớp điều kiện
     */
    @Override
    public <P> Page<P> findAllWithFilters(S searchDTO, Class<P> projectionClass) {
        // Sử dụng method reference this::buildPredicates, tự động khớp với chữ ký mới
        return findPageInternal(searchDTO, projectionClass, this::buildPredicates);
    }


    /**
     * Tìm tất cả các entity với bộ lọc và sắp xếp, không phân trang.
     * Phương thức này trả về danh sách đầy đủ các entity khớp điều kiện.
     *
     * @param searchDTO DTO chứa các điều kiện tìm kiếm và sắp xếp
     * @return Danh sách các entity khớp điều kiện
     */
    @Override
    public List<T> findAllWithFiltersAndSort(S searchDTO) {
        return this.findAllWithFiltersAndSort(searchDTO, this.domainClass);
    }

    /**
     * Tìm tất cả các entity với bộ lọc và sắp xếp, không phân trang, trả về dạng projection.
     * Phương thức này cho phép ánh xạ kết quả sang một lớp DTO khác để tối ưu hiệu năng.
     *
     * @param searchDTO       DTO chứa các điều kiện tìm kiếm và sắp xếp
     * @param projectionClass Lớp DTO để ánh xạ kết quả
     * @return Danh sách các DTO projection khớp điều kiện
     */
    @Override
    public <P> List<P> findAllWithFiltersAndSort(S searchDTO, Class<P> projectionClass) {
        return findListInternal(searchDTO, projectionClass, this::buildPredicates);
    }

    /**
     * Phương thức nội bộ để tìm danh sách entity có phân trang.
     * Đây là phương thức helper chung cho việc tìm kiếm có phân trang.
     *
     * @param searchDTO        DTO chứa điều kiện tìm kiếm và phân trang
     * @param resultClass      Lớp kết quả (entity hoặc DTO)
     * @param predicateBuilder Function để xây dựng các điều kiện WHERE
     * @return Danh sách entity/DTO khớp điều kiện
     */
    protected <P> List<P> findListInternal(S searchDTO, Class<P> resultClass, QuadFunction<Root<T>, CriteriaQuery<?>, CriteriaBuilder, S, List<Predicate>> predicateBuilder) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<P> cq = cb.createQuery(resultClass);
        Root<T> root = cq.from(domainClass);

        cq.select(createProjection(resultClass, root, cb));

        // CẬP NHẬT: Gọi predicateBuilder với 4 tham số
        List<Predicate> predicates = predicateBuilder.apply(root, cq, cb, searchDTO);
        cq.where(predicates.toArray(new Predicate[0]));

        applySort(cq, root, createSortFromDto(searchDTO));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Phương thức nội bộ để tìm danh sách entity có phân trang.
     * Đây là phương thức helper chung cho việc tìm kiếm có phân trang.
     *
     * @param searchDTO        DTO chứa điều kiện tìm kiếm và phân trang
     * @param resultClass      Lớp kết quả (entity hoặc DTO)
     * @param predicateBuilder Function để xây dựng các điều kiện WHERE
     * @return Trang chứa entity/DTO khớp điều kiện
     */
    protected <P> Page<P> findPageInternal(S searchDTO, Class<P> resultClass, QuadFunction<Root<T>, CriteriaQuery<?>, CriteriaBuilder, S, List<Predicate>> predicateBuilder) {
        Pageable pageable = createPageableFromDto(searchDTO);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ---- Query để lấy dữ liệu ----
        CriteriaQuery<P> dataQuery = cb.createQuery(resultClass);
        Root<T> dataRoot = dataQuery.from(domainClass);
        dataQuery.select(createProjection(resultClass, dataRoot, cb));

        // CẬP NHẬT: Gọi predicateBuilder với 4 tham số
        List<Predicate> predicates = predicateBuilder.apply(dataRoot, dataQuery, cb, searchDTO);
        dataQuery.where(predicates.toArray(new Predicate[0]));

        applySort(dataQuery, dataRoot, pageable.getSort());

        List<P> resultList = entityManager.createQuery(dataQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // ---- Query để đếm tổng số bản ghi ----
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(domainClass);
        countQuery.select(cb.count(countRoot));

        // CẬP NHẬT: Gọi predicateBuilder với 4 tham số
        List<Predicate> countPredicates = predicateBuilder.apply(countRoot, countQuery, cb, searchDTO);
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    /**
     * Tạo đối tượng Pageable từ DTO tìm kiếm.
     * Phương thức này xử lý chuyển đổi từ 1-based page number sang 0-based cho Spring.
     *
     * @param searchDTO DTO chứa thông tin phân trang
     * @return Đối tượng Pageable
     */
    protected Pageable createPageableFromDto(S searchDTO) {
        if (searchDTO == null) {
            return PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
        }

        // Spring Pageable is 0-indexed, but frontend APIs often use 1-indexed pages.
        // We assume the DTO receives a 1-based page number.
        int page = searchDTO.getPage() != null && searchDTO.getPage() > 0 ? searchDTO.getPage() - 1 : DEFAULT_PAGE;
        int size = searchDTO.getSize() != null && searchDTO.getSize() > 0 ? searchDTO.getSize() : DEFAULT_PAGE_SIZE;

        Sort sort = createSortFromDto(searchDTO);
        return PageRequest.of(page, size, sort);
    }

    /**
     * Creates a selection for a CriteriaQuery, either the full entity root or a constructor projection.
     *
     * @param projectionClass The class to project to. If it's the domain class, the root is returned.
     * @param root            The root of the query.
     * @param cb              The CriteriaBuilder.
     * @param <P>             The type of the projection.
     * @return A selection object for the query.
     */
    @SuppressWarnings("unchecked")
    protected <P> Selection<P> createProjection(Class<P> projectionClass, Root<T> root, CriteriaBuilder cb) {
        // Nếu lớp yêu cầu là lớp entity, trả về root (SELECT *)
        if (projectionClass == this.domainClass) {
            // This cast is safe. The 'if' condition ensures that type P is the same as type T.
            // Root<T> is a subtype of Selection<T>, so casting it to Selection<P>
            // (which is effectively Selection<T> in this branch) is a valid upcast.
            // The compiler warns because it cannot link the Class object equality
            // to the generic type parameters P and T.
            return (Selection<P>) root;
        }

        // Nếu là DTO/VO, xây dựng cb.construct
        List<Selection<?>> selections = new ArrayList<>();
        try {
            // Lấy constructor khớp với các trường của DTO
            Field[] fields = projectionClass.getDeclaredFields();
            Class<?>[] constructorArgTypes = new Class<?>[fields.length];
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                selections.add(root.get(field.getName()));
                constructorArgTypes[i] = field.getType();
            }
            // Kiểm tra sự tồn tại của constructor để báo lỗi sớm
            projectionClass.getConstructor(constructorArgTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Projection class " + projectionClass.getSimpleName() +
                    " must have an all-args constructor matching its declared fields.", e);
        } catch (IllegalArgumentException e) {
            // Lỗi này xảy ra khi root.get(fieldName) không tìm thấy trường trong entity
            throw new IllegalStateException("A field in projection class " + projectionClass.getSimpleName() +
                    " does not exist in the entity " + domainClass.getSimpleName() + ". " + e.getMessage(), e);
        }

        return cb.construct(projectionClass, selections.toArray(new Selection[0]));
    }


    /**
     * Áp dụng sắp xếp cho CriteriaQuery.
     * Phương thức này chuyển đổi Spring Sort thành JPA Order.
     *
     * @param cq   CriteriaQuery để áp dụng sắp xếp
     * @param root Root của query
     * @param sort Đối tượng Sort
     */
    protected void applySort(CriteriaQuery<?> cq, Root<T> root, Sort sort) {
        if (sort.isSorted()) {
            List<Order> orders = sort.stream()
                    .map(order -> order.isAscending() ?
                            entityManager.getCriteriaBuilder().asc(root.get(order.getProperty())) :
                            entityManager.getCriteriaBuilder().desc(root.get(order.getProperty())))
                    .collect(Collectors.toList());
            cq.orderBy(orders);
        }
    }

    /**
     * Tạo đối tượng Sort từ DTO tìm kiếm.
     * Phương thức này xử lý thông tin sắp xếp từ DTO.
     *
     * @param searchDTO DTO chứa thông tin sắp xếp
     * @return Đối tượng Sort
     */
    protected Sort createSortFromDto(S searchDTO) {
        if (searchDTO != null && StringUtils.hasText(searchDTO.getSortBy())) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(searchDTO.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            return Sort.by(direction, searchDTO.getSortBy());
        }
        return Sort.unsorted();
    }


    /**
     * Xóa mềm một entity theo ID.
     * <p>
     * Sử dụng CriteriaUpdate để cập nhật trực tiếp trong DB, hiệu quả hơn việc đọc và ghi lại entity.
     *
     * @param id UUID của entity cần xóa mềm.
     * @return Số lượng bản ghi đã được cập nhật (0 hoặc 1).
     */
    @Override
    public int softDeleteById(UUID id) {
        if (id == null) {
            return 0;
        }
        return this.softDeleteByIds(List.of(id));
    }

    /**
     * Xóa mềm hàng loạt entity theo danh sách ID.
     * <p>
     * Phương thức này sử dụng CriteriaUpdate để thực hiện một câu lệnh UPDATE duy nhất,
     * giúp tối ưu hiệu năng. Nó sẽ cập nhật các trường auditing isDelete, deleteTime, và updateTime.
     * <p>
     * LƯU Ý QUAN TRỌNG: Do sử dụng CriteriaUpdate, các callback của JPA EntityListener (ví dụ: AuditingEntityListener)
     * sẽ không được tự động kích hoạt. Do đó, các trường audit như `deleteBy` và `updateBy`
     * cần được thiết lập một cách thủ công nếu cần.
     *
     * @param ids Danh sách UUID của các entity cần xóa mềm.
     * @return Số lượng bản ghi đã được cập nhật.
     */
    @Override
    public int softDeleteByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<T> updateQuery = cb.createCriteriaUpdate(domainClass);
        Root<T> root = updateQuery.from(domainClass);

        Instant now = Instant.now();
        UUID currentUserId = getCurrentUserIdOrAnonymous();

        updateQuery.set(root.get("isDelete"), DelFlag.DELETED.getValue());
        updateQuery.set(root.get("deleteTime"), now);
        updateQuery.set(root.get("deleteBy"), currentUserId);
        updateQuery.set(root.get("updateTime"), now);
        updateQuery.set(root.get("updateBy"), currentUserId);
        updateQuery.where(root.get("id").in(ids));

        return entityManager.createQuery(updateQuery).executeUpdate();
    }

    private static final UUID ANONYMOUS_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private UUID getCurrentUserIdOrAnonymous() {
        try {
            return com.example.CineBook.common.security.SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            return ANONYMOUS_USER_ID;
        }
    }


    // ========= PRIVATE HELPER METHODS ============
    @FunctionalInterface
    protected interface QuadFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    /**
     * Cập nhật một trường cho tất cả các entity khớp với bộ lọc.
     *
     * @param searchDTO DTO chứa các điều kiện lọc.
     * @param fieldName Tên trường cần cập nhật.
     * @param value     Giá trị mới cho trường.
     * @return Số lượng bản ghi đã được cập nhật.
     */
    @Override
    @Transactional
    public int updateFieldByFilter(S searchDTO, String fieldName, Object value) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<T> updateQuery = cb.createCriteriaUpdate(domainClass);
        Root<T> root = updateQuery.from(domainClass);

        // Thiết lập giá trị cập nhật
        updateQuery.set(root.get(fieldName), value);
        // Có thể thêm cả updateTime nếu cần
        // updateQuery.set(root.get("updateTime"), Instant.now());

        // Áp dụng các điều kiện lọc
        List<Predicate> predicates = buildPredicates(root, null, cb, searchDTO); // null vì CriteriaUpdate không có query riêng
        if (predicates.isEmpty()) {
            // Tránh cập nhật toàn bộ bảng nếu không có bộ lọc nào được chỉ định
            throw new IllegalArgumentException("Cần có ít nhất một điều kiện lọc để cập nhật hàng loạt.");
        }
        updateQuery.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(updateQuery).executeUpdate();
    }
}
