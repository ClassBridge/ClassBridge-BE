package com.linked.classbridge.repository;

import com.linked.classbridge.domain.OneDayClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OneDayClassRepository extends JpaRepository<OneDayClass, Long> {
}
