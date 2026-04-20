package com.example.juniorinterview.repository;

import com.example.juniorinterview.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIdGreaterThanOrderByIdAsc(Long afterId, Pageable pageable);
}
