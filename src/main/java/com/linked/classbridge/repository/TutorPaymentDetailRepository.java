package com.linked.classbridge.repository;

import com.linked.classbridge.domain.TutorPayment;
import com.linked.classbridge.domain.TutorPaymentDetail;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorPaymentDetailRepository extends JpaRepository<TutorPaymentDetail, Long> {
    Optional<List<TutorPaymentDetail>> findByTutorPayment(TutorPayment tutorPayment);
}
