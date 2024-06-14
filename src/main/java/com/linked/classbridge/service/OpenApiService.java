package com.linked.classbridge.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class OpenApiService {

    @Value("${openapi.key}")
    private String apikey;

    private static final String baseUrl = "https://api.odcloud.kr/api/nts-businessman/v1/status";

    public boolean validate(String businessRegistrationNumber) throws URISyntaxException {

        log.info("validate business registration number: {}", businessRegistrationNumber);

        List<String> array = new ArrayList<>();
        array.add(businessRegistrationNumber);
        JSONObject requestBody = new JSONObject();
        requestBody.put("b_no", array);

        JSONObject response = getApiResponse(requestBody);

        if (response == null) {
            // HTTP 요청이 실패하거나 응답 본문이 없는 경우
            return false;
        }

        try {
            JSONArray data = (JSONArray) response.get("data");
            if (!data.isEmpty()) {
                JSONObject firstItem = (JSONObject) data.get(0);
                String b_stt_cd = (String) firstItem.get("b_stt_cd");
                if (!"01".equals(b_stt_cd)) {
                    log.info("Not a valid business registration number: {}", businessRegistrationNumber);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Parsing response failed", e);
            return false;
        }

        log.info("Valid business registration number: {}", businessRegistrationNumber);
        return true;
    }

    public JSONObject getApiResponse(JSONObject requestBody) throws URISyntaxException {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        URI uri = null;
        uri = new URI(baseUrl + "?serviceKey=" + apikey);

        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            log.error("Exception in getApiResponse: {}", e.getMessage());
            return null;
        }

        String response = responseEntity.getBody();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        try {
            return (JSONObject) parser.parse(response);
        } catch (ParseException e) {
            log.error("Failed to parse response to JSON", e);
            return null;
        }
    }
}