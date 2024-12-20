package com.example.demo.repository;

import com.example.demo.entity.Category;
import com.example.demo.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Integer> {

    Category findByCategoryName(String name);

    @Query("""
            SELECT c.categoryId from Category c
            WHERE c.categoryName = :categoryName
            """)
    Integer getIdByCategoryName(@Param("categoryName")String categoryName);
}
