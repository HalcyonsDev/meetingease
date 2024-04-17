package ru.halcyon.meetingease.api.dadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.halcyon.meetingease.exception.WrongDataException;
import ru.halcyon.meetingease.model.support.Address;

@Component
public class OSMNominatiumAPI {
    private static final String BASE_URL = "https://nominatim.openstreetmap.org/search";

    public Address getCorrectAddress(String city, String street, String houseNumber) {
        RestTemplate restTemplate = new RestTemplate();

        String query = String.format("%s %s %s", city, street, houseNumber);
        String url = String.format("%s?q=%s&format=json&addressdetails=1&limit=1&polygon_svg=1", BASE_URL, query);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        HttpStatusCode status = response.getStatusCode();

        if (!status.is2xxSuccessful()) {
            throw new WrongDataException("Something went wrong.");
        }

        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode jsonNode = mapper.readTree(responseBody);
            isValid(jsonNode);

            JsonNode addressJSON = jsonNode.get(0).get("address");
            Address address = Address.builder()
                    .region(addressJSON.get("region").toString())
                    .city(addressJSON.get("city").toString())
                    .street(addressJSON.get("road").toString())
                    .houseNumber(addressJSON.get("city").toString())
                    .displayName(jsonNode.get(0).get("display_name").toString())
                    .build();

            return address;
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void isValid(JsonNode jsonNode) {
        try {
            jsonNode.get(0).get("address").get("house_number");
        } catch (Exception ignored) {
            throw new WrongDataException("Please specify the correct house for the meeting.");
        }
    }
}
