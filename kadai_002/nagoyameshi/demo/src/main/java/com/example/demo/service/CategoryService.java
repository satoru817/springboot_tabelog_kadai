package com.example.demo.service;

import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;


    public List<Category> fetchAll() {
        return categoryRepository.findAll();
    }

    public void create(Category category){
        categoryRepository.save(category);
    }

    public void update(Category category){
        categoryRepository.save(category);
    }

    public void delete(Integer categoryId){
        categoryRepository.deleteById(categoryId);
    }
}
