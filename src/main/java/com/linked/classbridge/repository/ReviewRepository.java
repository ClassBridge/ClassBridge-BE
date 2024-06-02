package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.Review;
import com.linked.classbridge.domain.User;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByLessonAndUser(Lesson lesson, User user);

    Slice<Review> findByOneDayClass(OneDayClass oneDayClass, Pageable pageable);
}
