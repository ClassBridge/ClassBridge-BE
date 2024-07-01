package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.domain.Category;

public interface OneDayClassProjection {

    Long getClassId();
    Double getAverageAge();
    Long getMaleCount();
    Long getFemaleCount();
    Category getCategory();
}
