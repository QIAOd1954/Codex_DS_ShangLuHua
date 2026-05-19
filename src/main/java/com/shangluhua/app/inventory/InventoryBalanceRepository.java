package com.shangluhua.app.inventory;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, Long> {
    Optional<InventoryBalance> findByWarehouseCodeAndSkuId(String warehouseCode, Long skuId);

    List<InventoryBalance> findByQuantityLessThan(int threshold);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from InventoryBalance b where b.warehouseCode = :warehouseCode and b.sku.id = :skuId")
    Optional<InventoryBalance> lockByWarehouseCodeAndSkuId(@Param("warehouseCode") String warehouseCode, @Param("skuId") Long skuId);
}
