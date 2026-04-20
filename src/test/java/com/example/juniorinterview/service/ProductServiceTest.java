package com.example.juniorinterview.service;

import com.example.juniorinterview.dto.CursorResponse;
import com.example.juniorinterview.exception.ResourceNotFoundException;
import com.example.juniorinterview.model.Product;
import com.example.juniorinterview.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findById_returnsProduct_whenProductExists() {
        Product product = product(1L, "Keyboard", "Mechanical", 99.0, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.findById(1L);

        assertThat(result).isSameAs(product);
    }

    @Test
    void findById_throwsException_whenProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id 99");
    }

    @Test
    void patch_updatesOnlyProvidedFields() {
        Product existing = product(1L, "Mouse", "Wireless", 25.0, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.patch(1L, Map.of("price", 30.5, "quantity", 7));

        assertThat(updated.getName()).isEqualTo("Mouse");
        assertThat(updated.getDescription()).isEqualTo("Wireless");
        assertThat(updated.getPrice()).isEqualTo(30.5);
        assertThat(updated.getQuantity()).isEqualTo(7);
        verify(productRepository).save(existing);
    }

    @Test
    void findByCursor_returnsNextCursor_whenPageIsFull() {
        Product first = product(1L, "A", "A", 10.0, 1);
        Product second = product(2L, "B", "B", 20.0, 2);
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second)));

        CursorResponse<Product> response = productService.findByCursor(null, 2);

        assertThat(response.items()).containsExactly(first, second);
        assertThat(response.nextCursor()).isEqualTo(2L);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void findByCursor_capsLimitAt100() {
        when(productRepository.findByIdGreaterThanOrderByIdAsc(eq(10L), any(Pageable.class)))
                .thenReturn(List.of());

        productService.findByCursor(10L, 200);

        verify(productRepository).findByIdGreaterThanOrderByIdAsc(eq(10L), any(Pageable.class));
    }

    private Product product(Long id, String name, String description, Double price, Integer quantity) {
        Product product = new Product(name, description, price, quantity);
        product.setId(id);
        return product;
    }
}
