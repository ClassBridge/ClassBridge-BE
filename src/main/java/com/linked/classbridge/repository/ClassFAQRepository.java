package com.linked.classbridge.repository;

import com.linked.classbridge.domain.ClassFAQ;
import com.linked.classbridge.domain.OneDayClass;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassFAQRepository extends JpaRepository<ClassFAQ, Long> {
    void deleteAllByOneDayClass(OneDayClass oneDayClass);

    List<ClassFAQ> findAllByOneDayClassClassId(Long classId);

    void deleteAllByOneDayClassClassId(long classId);
}
