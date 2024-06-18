package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Reservation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByLesson(Lesson lesson);
}
