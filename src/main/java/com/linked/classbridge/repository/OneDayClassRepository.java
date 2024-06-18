package com.linked.classbridge.repository;

import com.linked.classbridge.domain.OneDayClass;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OneDayClassRepository extends JpaRepository<OneDayClass, Long> {
    Page<OneDayClass> findAllByTutorUserId(long userId, Pageable pageable);

    Page<OneDayClass> findAllByClassIdIn(List<Long> list, Pageable pageable);
}
