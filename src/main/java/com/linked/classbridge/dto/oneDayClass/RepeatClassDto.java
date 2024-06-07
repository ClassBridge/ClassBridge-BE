package com.linked.classbridge.dto.oneDayClass;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class RepeatClassDto {
    private DayOfWeek dayOfWeek;
    private List<LocalTime> times;
}
