package com.linked.classbridge.dto.kakaoMapDto;


import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Map;
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
public class KakaoMapResponse {
    private KakaoAddress address;
    private String address_name;
    private String address_type;
    private KakaoRoadAddress road_address;
    private String x;
    private String y;

    public KakaoMapResponse(ArrayList<Map<String, Object>> documents) {
        Map<String, Object> document = documents.get(0);

        this.address_name = (String) document.get("address_name");
        this.address_type = (String) document.get("address_type");
        this.x = (String) document.get("x");
        this.y = (String) document.get("y");

        Gson gson = new Gson();

        // address 정보 파싱
        String addressJson = gson.toJson(document.get("address"));
        this.address = gson.fromJson(addressJson, KakaoAddress.class);

        // road_address 정보 파싱
        String roadAddressJson = gson.toJson(document.get("road_address"));
        this.road_address = gson.fromJson(roadAddressJson, KakaoRoadAddress.class);
    }
}
