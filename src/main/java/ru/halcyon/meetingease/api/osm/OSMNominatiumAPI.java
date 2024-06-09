package ru.halcyon.meetingease.api.osm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.halcyon.meetingease.exception.InvalidCredentialsException;
import ru.halcyon.meetingease.support.Address;

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
            throw new InvalidCredentialsException("Something went wrong.");
        }

        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode jsonNode = mapper.readTree(responseBody);
            isValid(jsonNode);

            JsonNode addressJSON = jsonNode.get(0).get("address");
            String addressRegion = addressJSON.get("region").toString();
            String addressCity = addressJSON.get("city").toString();
            String addressStreet = addressJSON.get("road").toString();
            String addressHouseNumber = addressJSON.get("house_number").toString();
            String displayName = jsonNode.get(0).get("display_name").toString();

            return Address.builder()
                    .region(addressRegion.substring(1, addressRegion.length() - 1))
                    .city(addressCity.substring(1, addressCity.length() - 1))
                    .street(addressStreet.substring(1, addressStreet.length() - 1))
                    .houseNumber(addressHouseNumber.substring(1, addressHouseNumber.length() - 1))
                    .displayName(displayName.substring(1, displayName.length() - 1))
                    .build();
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void isValid(JsonNode jsonNode) {
        try {
            jsonNode.get(0).get("address").get("house_number");
        } catch (Exception ignored) {
            throw new InvalidCredentialsException("Please specify the correct house for the meeting.");
        }
    }
}
