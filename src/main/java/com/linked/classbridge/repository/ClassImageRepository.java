package com.linked.classbridge.repository;

import com.linked.classbridge.domain.ClassImage;
import com.linked.classbridge.domain.OneDayClass;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassImageRepository extends JpaRepository<ClassImage, Long> {

    List<ClassImage> findAllByOneDayClassClassIdInAndSequence(List<Long> classIdList, int sequence);

    void deleteAllByOneDayClassClassId(long classId);

    List<ClassImage> findAllByOneDayClassClassId(long classId);

    List<ClassImage> findAllByOneDayClassClassIdOrderBySequence(long classId);

    Optional<ClassImage> findFirstByOneDayClassClassIdAndSequence(Long classId, int sequence);
}
