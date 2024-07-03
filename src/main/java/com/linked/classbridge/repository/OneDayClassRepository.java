package com.linked.classbridge.repository;

import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.dto.oneDayClass.OneDayClassProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OneDayClassRepository extends JpaRepository<OneDayClass, Long> {
    Page<OneDayClass> findAllByTutorUserId(long userId, Pageable pageable);

    Page<OneDayClass> findAllByClassIdIn(List<Long> list, Pageable pageable);

    @Query("SELECT c.classId as classId, c.averageAge as averageAge, c.maleCount as maleCount, c.femaleCount as femaleCount, c.category as category FROM OneDayClass c")
    List<OneDayClassProjection> findAllWithSelectedColumns();

    @Query("SELECT c.classId FROM OneDayClass c ORDER BY c.totalStarRate DESC, c.totalWish DESC")
    List<Long> getTopClassesId(Pageable pageable);

    @Query("SELECT u.nickname FROM OneDayClass o JOIN o.tutor u WHERE o.classId = :classId")
    String findTutorNameByClassId(@Param("classId") Long classId);
}

