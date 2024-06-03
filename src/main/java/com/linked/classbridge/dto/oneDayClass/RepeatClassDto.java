package com.linked.classbridge.dto.oneDayClass;

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
    private dayList mon;
    private dayList tue;
    private dayList wed;
    private dayList thu;
    private dayList fri;
    private dayList sat;
    private dayList sun;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class dayList {
        private List<LocalTime> times;
        private int personal;
    }
}
