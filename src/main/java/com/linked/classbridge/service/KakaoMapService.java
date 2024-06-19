package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.KAKAO_MAP_ERROR;

import com.linked.classbridge.config.KakaoMapConfig;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.dto.kakaoMapDto.KakaoMapResponse;
import com.linked.classbridge.exception.RestApiException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoMapService {
    private final KakaoMapConfig kakaoMapConfig;
    private final RestTemplate restTemplate;

    public void extracted(OneDayClass oneDayClass) {
        try {
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(this.getHeader());

            UriComponents uriComponents = UriComponentsBuilder.fromUriString(kakaoMapConfig.getMapUrl())
                    .queryParam("analyze_type", "similar")
                    .queryParam("page", "1")
                    .queryParam("size", "10")
                    .queryParam("query", oneDayClass.getAddress1() + " " + oneDayClass.getAddress2() + " " + oneDayClass.getAddress3())
                    .encode(StandardCharsets.UTF_8) // UTF-8로 인코딩
                    .build();

            URI targetUrl = uriComponents.toUri();
            ResponseEntity<Map> responseEntity = restTemplate.exchange(targetUrl, HttpMethod.GET, requestEntity, Map.class);
            KakaoMapResponse kakaoMapResponse = new KakaoMapResponse((ArrayList)responseEntity.getBody().get("documents"));
            oneDayClass.setAddress1(kakaoMapResponse.getRoad_address().getRegion_1depth_name());
            oneDayClass.setAddress2(kakaoMapResponse.getRoad_address().getRegion_2depth_name());
            oneDayClass.setLatitude(Double.parseDouble(kakaoMapResponse.getY()));
            oneDayClass.setLongitude(Double.parseDouble(kakaoMapResponse.getX()));

        } catch (HttpClientErrorException e) {
            throw new RestApiException(KAKAO_MAP_ERROR);
        }
    }

    private HttpHeaders getHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        String auth = "KakaoAK " + kakaoMapConfig.getAdminKey();

        httpHeaders.set("Authorization", auth);

        return httpHeaders;
    }
}
