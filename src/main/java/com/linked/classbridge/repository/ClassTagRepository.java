package com.linked.classbridge.repository;

import com.linked.classbridge.domain.ClassTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassTagRepository extends JpaRepository<ClassTag, Long> {
    List<ClassTag> findAllByOneDayClassClassId(Long classId);

    void deleteAllByOneDayClassClassId(long classId);
}
