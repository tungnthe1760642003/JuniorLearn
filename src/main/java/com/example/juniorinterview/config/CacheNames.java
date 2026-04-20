package com.example.juniorinterview.config;

// Lưu tên cache constants để tránh hardcode string ở @Cacheable, @CacheEvict.
// Nếu đổi tên cache, chỉ cần sửa 1 chỗ duy nhất ở đây.
public final class CacheNames {

    public static final String PRODUCTS = "products";
    public static final String PRODUCT_BY_ID = "productById";

    private CacheNames() {
        // Utility class: không cần tạo object, chỉ dùng static constants.
        // Private constructor để ngăn tạo instance của class này.
    }
}
