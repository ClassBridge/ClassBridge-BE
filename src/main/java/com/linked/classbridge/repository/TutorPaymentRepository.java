package com.linked.classbridge.repository;

import com.linked.classbridge.domain.TutorPayment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorPaymentRepository extends JpaRepository<TutorPayment, Long> {
    Optional<List<TutorPayment>> findByUserId(Long userId);
}
