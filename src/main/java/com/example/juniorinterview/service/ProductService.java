package com.example.juniorinterview.service;

import com.example.juniorinterview.config.CacheNames;
import com.example.juniorinterview.dto.CursorResponse;
import com.example.juniorinterview.exception.ResourceNotFoundException;
import com.example.juniorinterview.model.Product;
import com.example.juniorinterview.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(cacheNames = CacheNames.PRODUCTS)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // Ví dụ cursor pagination theo chuẩn production.
    // Lấy sản phẩm có id > afterId, sắp xếp tăng dần theo id.
    // Trả về đúng 'limit' phần tử và nextCursor cho trang tiếp theo.
    public CursorResponse<Product> findByCursor(Long afterId, int limit) {
        // Giới hạn kích thước trang hợp lý để tránh requests quá lớn.
        limit = Math.max(1, Math.min(limit, 100));
        Pageable pageable = PageRequest.of(0, limit, Sort.by("id").ascending());
        List<Product> items;

        if (afterId == null) {
            // Trang đầu tiên: lấy limit sản phẩm đầu tiên từ đầu bảng.
            items = productRepository.findAll(pageable).getContent();
        } else {
            // Trang sau: lấy các sản phẩm có id > afterId.
            items = productRepository.findByIdGreaterThanOrderByIdAsc(afterId, pageable);
        }

        Long nextCursor = null;
        boolean hasNext = false;
        if (items.size() == limit) {
            nextCursor = items.get(items.size() - 1).getId();
            hasNext = true;
        }

        return new CursorResponse<>(items, nextCursor, hasNext);
    }

    @Cacheable(cacheNames = CacheNames.PRODUCT_BY_ID, key = "#id")
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    @CacheEvict(cacheNames = CacheNames.PRODUCTS, allEntries = true)
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.PRODUCTS, allEntries = true)
    })
    public Product update(Long id, Product product) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setQuantity(product.getQuantity());

        return productRepository.save(existing);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.PRODUCTS, allEntries = true)
    })
    public Product patch(Long id, Map<String, Object> changes) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));

        if (changes.containsKey("name")) {
            existing.setName((String) changes.get("name"));
        }
        if (changes.containsKey("description")) {
            existing.setDescription((String) changes.get("description"));
        }
        if (changes.containsKey("price")) {
            existing.setPrice(Double.valueOf(changes.get("price").toString()));
        }
        if (changes.containsKey("quantity")) {
            existing.setQuantity(Integer.valueOf(changes.get("quantity").toString()));
        }

        return productRepository.save(existing);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.PRODUCTS, allEntries = true)
    })
    public void delete(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));

        productRepository.delete(existing);
    }
}
