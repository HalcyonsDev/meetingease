package ru.halcyon.meetingease.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.SerializationFeature;
import ru.halcyon.meetingease.dto.ClientUpdateDto;
import ru.halcyon.meetingease.dto.CompanyCreateDto;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Company;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.repository.CompanyRepository;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.service.company.CompanyService;
import ru.halcyon.meetingease.support.Role;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class ClientControllerTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ClientService clientService;

    private static final String CLIENT_EMAIL = "test_email@gmail.com";

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void uploadPhoto() throws Exception {
        createClient(CLIENT_EMAIL);
        setJwtAuth();

        MockMultipartFile mockFile = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "dummy image content".getBytes());

        mvc.perform(multipart("/api/v1/clients/upload-photo")
                .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("photo").isNotEmpty());
    }

    @Test
    void updateData() throws Exception {
        createClient(CLIENT_EMAIL);
        setJwtAuth();

        ClientUpdateDto dto = getClientUpdateDto();

        ResultActions result = mvc.perform(patch("/api/v1/clients/update-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        Client client = clientService.findByEmail(dto.getEmail());
        result.andExpect(content().json(getRequestJson(client)));
    }

    @Test
    void updateRole() throws Exception {
        createClient(CLIENT_EMAIL);
        setJwtAuth();
        Company company = createCompany();
        Client client = createClient("some_email@gmail.com");
        companyService.addClient(company.getId(), client.getEmail());

        mvc.perform(patch("/api/v1/clients/{clientId}/update-role", client.getId())
                .param("role", String.valueOf(Role.USER)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("role").value(String.valueOf(Role.USER)));
    }

    private Client createClient(String email) {
        return clientRepository.save(
                Client.builder()
                        .email(email)
                        .name("test_name")
                        .surname("test_surname")
                        .password("test_password")
                        .position("test_position")
                        .phoneNumber("test_number")
                        .isVerified(true)
                        .role(Role.ADMIN)
                        .photo("test_photo")
                        .build()
        );
    }

    private Company createCompany() {
        return companyService.create(
                new CompanyCreateDto("test-name", "test-description")
        );
    }

    private void setJwtAuth() {
        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, CLIENT_EMAIL, true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
    }

    private ClientUpdateDto getClientUpdateDto() {
        return new ClientUpdateDto("new-name", "new-surname", "new_email@gmail.com", "+123456", "new-position");
    }

    private String getRequestJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();

        return writer.writeValueAsString(object);
    }
}
