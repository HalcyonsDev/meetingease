package ru.halcyon.meetingease.api.dadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.halcyon.meetingease.exception.WrongDataException;

@Component
public class DadataAPI {
    @Value("${dadata.api_key}")
    private String apiKey;

    @Value("${dadata.secret_key}")
    private String secretKey;

    public String getCorrectAddress(String query) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Token " + apiKey);
        headers.set("X-Secret", secretKey);

        String json = String.format("[\"%s\"]", query);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://cleaner.dadata.ru/api/v1/clean/address",
                HttpMethod.POST,
                request,
                String.class
        );

        HttpStatusCode status = response.getStatusCode();

        if (!status.is2xxSuccessful()) {
            throw new WrongDataException("Something went wrong.");
        }

        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode jsonNode = mapper.readTree(responseBody);
            isValid(jsonNode);

            String address = jsonNode.get(0).get("result").toString();
            return address.substring(1, address.length() - 1);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void isValid(JsonNode jsonNode) {
        if (jsonNode.get(0).get("city") == null) {
            throw new WrongDataException("Please specify the city for the meeting.");
        }

        if (jsonNode.get(0).get("street") == null) {
            throw new WrongDataException("Please specify the street for the meeting.");
        }

        if (jsonNode.get(0).get("house") == null) {
            throw new WrongDataException("Please specify the house for the meeting.");
        }
    }
}
