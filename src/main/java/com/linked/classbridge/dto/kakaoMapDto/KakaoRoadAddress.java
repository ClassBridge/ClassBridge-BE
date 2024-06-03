package com.linked.classbridge.dto.kakaoMapDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class KakaoRoadAddress {
    private String address_name;
    private String building_name;
    private String main_building_no;
    private String region_1depth_name;
    private String region_2depth_name;
    private String region_3depth_name;
    private String region_3depth_h_name;
    private String road_name;
    private String sub_building_no;
    private String underground_yn;
    private String x;
    private String y;
    private String zone_no;
}

