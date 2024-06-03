package com.linked.classbridge.service;

import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OneDayClassService {

    private final OneDayClassRepository oneDayClassRepository;

    public OneDayClass findClassById(Long classId) {
        return oneDayClassRepository.findById(classId)
                .orElseThrow(() -> new RestApiException(ErrorCode.CLASS_NOT_FOUND));
    }
}
