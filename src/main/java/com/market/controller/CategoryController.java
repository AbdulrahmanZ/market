package com.market.controller;

import com.market.model.Category;
import com.market.service.AuthenticationService;
import com.market.service.CategoryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    AuthenticationService authenticationService;


    @PostMapping
    @Transactional
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        if (!authenticationService.getCurrentUser().getAdmin())
            throw new RuntimeException("Unauthorized - Non-admin user");
        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.ok(createdCategory);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping
    public ResponseEntity<Page<Category>> getAllCategories(Pageable pageable) {
        Page<Category> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }


    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @RequestBody Category request) {
        if (!authenticationService.getCurrentUser().getAdmin())
            throw new RuntimeException("Unauthorized - Non-admin user");

        Category categoryDetails = new Category();
        categoryDetails.setName(request.getName());

        Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (!authenticationService.getCurrentUser().getAdmin())
            throw new RuntimeException("Unauthorized - Non-admin user");
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
