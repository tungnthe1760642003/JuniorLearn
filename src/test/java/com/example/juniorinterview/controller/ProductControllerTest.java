package com.example.juniorinterview.controller;

import com.example.juniorinterview.exception.GlobalExceptionHandler;
import com.example.juniorinterview.model.Product;
import com.example.juniorinterview.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void getAllProducts_returnsOk() throws Exception {
        Product product = new Product("Laptop", "Office", 1000.0, 3);
        product.setId(1L);
        when(productService.findAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void createProduct_returnsBadRequest_whenBodyInvalid() throws Exception {
        Product invalid = new Product("", "Office", null, -1);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/products"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void getProductsCursor_returnsBadRequest_whenLimitTooLarge() throws Exception {
        mockMvc.perform(get("/api/products/cursor")
                        .param("limit", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Query parameter 'limit' must be between 1 and 100"));
    }

    @Test
    void createProduct_returnsCreatedAndLocationHeader() throws Exception {
        Product created = new Product("Monitor", "4K", 250.0, 4);
        created.setId(5L);
        when(productService.create(org.mockito.ArgumentMatchers.any(Product.class))).thenReturn(created);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Product("Monitor", "4K", 250.0, 4))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Monitor"));
    }
}
