package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Review;
import com.linked.classbridge.domain.ReviewImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReviewOrderBySequenceAsc(Review review);
}
