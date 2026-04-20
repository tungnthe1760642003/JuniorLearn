package com.example.juniorinterview.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from CI/CD! Da vao duoc roi nhe Part 2!");
    }
}
