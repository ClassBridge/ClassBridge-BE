package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Attendance;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByReservationAndUser(Reservation reservation, User user);
}
