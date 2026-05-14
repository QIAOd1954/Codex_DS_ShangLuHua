package com.shangluhua.app.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSpuRepository extends JpaRepository<ProductSpu, Long> {
    Optional<ProductSpu> findByCode(String code);
    List<ProductSpu> findTop50ByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String code, String name);
}
