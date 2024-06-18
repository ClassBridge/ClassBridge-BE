package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Badge;
import com.linked.classbridge.domain.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    Optional<Badge> findByCategoryAndThreshold(Category category, int threshold);
}
