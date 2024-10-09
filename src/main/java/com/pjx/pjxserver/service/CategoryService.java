package com.pjx.pjxserver.service;

import com.pjx.pjxserver.domain.Category;
import com.pjx.pjxserver.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
}