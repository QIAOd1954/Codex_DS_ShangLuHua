package com.shangluhua.app.sales;

import java.util.List;
import java.time.Instant;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    List<SalesOrder> findByContactPhoneOrderByCreatedAtDesc(String contactPhone);

    List<SalesOrder> findTop100ByOrderByCreatedAtDesc();

    Page<SalesOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<SalesOrder> findByOrderNoContainingIgnoreCaseOrderByCreatedAtDesc(String orderNo);

    @Query("select o from SalesOrder o left join fetch o.customer where o.status = :status order by o.createdAt desc")
    List<SalesOrder> findByStatusOrderByCreatedAtDesc(@Param("status") SalesOrderStatus status);

    @Query("select o from SalesOrder o left join fetch o.customer where o.createdAt between :start and :end order by o.createdAt desc")
    List<SalesOrder> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("start") Instant start, @Param("end") Instant end);

    @Query("select o from SalesOrder o left join fetch o.customer where lower(o.orderNo) like lower(concat('%', :keyword, '%')) or lower(o.customer.name) like lower(concat('%', :keyword, '%')) order by o.createdAt desc")
    List<SalesOrder> searchByKeyword(@Param("keyword") String keyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from SalesOrder o left join fetch o.items where o.id = :id")
    Optional<SalesOrder> lockWithItems(@Param("id") Long id);
}
