package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Wish;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishRepository extends JpaRepository<Wish, Long> {
    List<Wish> findByUserUserId(Long userId);

    boolean existsByUserUserIdAndOneDayClassClassId(Long userId, Long classId);
}
