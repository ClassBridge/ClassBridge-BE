package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.type.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(CategoryType string);
}
