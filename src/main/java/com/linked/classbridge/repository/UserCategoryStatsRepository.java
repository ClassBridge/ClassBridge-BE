package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.domain.UserCategoryStats;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCategoryStatsRepository extends JpaRepository<UserCategoryStats, Long> {

    Optional<UserCategoryStats> findByUserAndCategory(User user, Category category);
}
