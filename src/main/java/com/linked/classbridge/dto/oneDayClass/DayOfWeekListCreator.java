package com.linked.classbridge.dto.oneDayClass;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DayOfWeekListCreator {
    public static Map<DayOfWeek, List<LocalDate>> createDayOfWeekLists(LocalDate startDate, LocalDate endDate) {
        Map<DayOfWeek, List<LocalDate>> dayOfWeekLists = new HashMap<>();
        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            dayOfWeekLists.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(date);
            date = date.plusDays(1);
        }

        return dayOfWeekLists;
    }
}
