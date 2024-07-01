package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Refund;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    @Query("SELECT r FROM Refund r WHERE r.payment.reservation.user.userId = :userId")
    List<Refund> findAllByUserId(@Param("userId") Long userId);
}
