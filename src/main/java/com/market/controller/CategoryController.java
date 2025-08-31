package com.market.controller;

import com.market.dto.CategoryRequest;
import com.market.model.Category;
import com.market.service.AuthenticationService;
import com.market.service.CategoryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;
    private final AuthenticationService authenticationService;

    public CategoryController(CategoryService categoryService, AuthenticationService authenticationService) {
        this.categoryService = categoryService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        authenticationService.adminUserCheck();

        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setCode(categoryRequest.getCode());

        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.ok(createdCategory);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Category> getCategoryByCode(@PathVariable String code) {
        Category category = categoryService.getCategoryByCode(code);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
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
        authenticationService.adminUserCheck();

        Category existedCategory = categoryService.getCategoryById(id);
        if (request.getName() != null) existedCategory.setName(request.getName());
        if (request.getCode() != null) existedCategory.setCode(request.getCode());

        Category updatedCategory = categoryService.updateCategory(id, existedCategory);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        authenticationService.adminUserCheck();
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
