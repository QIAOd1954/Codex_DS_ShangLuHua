package com.shangluhua.app.purchase;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findTop100ByOrderByCreatedAtDesc();

    List<PurchaseOrder> findByOrderNoContainingIgnoreCaseOrderByCreatedAtDesc(String orderNo);

    List<PurchaseOrder> findByStatusOrderByCreatedAtDesc(PurchaseOrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PurchaseOrder o left join fetch o.items where o.id = :id")
    Optional<PurchaseOrder> lockWithItems(@Param("id") Long id);
}
