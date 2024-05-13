package ru.halcyon.meetingease.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.SerializationFeature;
import ru.halcyon.meetingease.dto.CompanyCreateDto;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Company;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.repository.CompanyRepository;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.company.CompanyService;
import ru.halcyon.meetingease.support.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
public class CompanyControllerTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompanyService companyService;

    private static final String CLIENT_EMAIL = "test_email@gmail.com";
    private static final String OWNER_EMAIL = "owner_email@gmail.com";

    @BeforeEach
    public void setUp() {
        clientRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void create() throws Exception {
        Client client = createClient(OWNER_EMAIL);
        setJwtAuth();

        CompanyCreateDto dto = getCompanyDto();

        mvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(dto)))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name").value(dto.getName()))
                .andExpect(jsonPath("description").value(dto.getDescription()))
                .andExpect(jsonPath("clients[0]").value(client));
    }

    @Test
    void createValidatesData() throws Exception {
        createClient(OWNER_EMAIL);
        setJwtAuth();

        CompanyCreateDto dto = new CompanyCreateDto("invalid_name", "test_description");

        mvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(dto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("title").value("Bad Request"))
                .andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("detail").value("Name must contain only letters, digits, and dashes"));
    }

    @Test
    void addClient() throws Exception {
        createClient(OWNER_EMAIL);
        setJwtAuth();
        Company company = companyService.create(getCompanyDto());

        Client client = createClient(CLIENT_EMAIL);

        ResultActions result = mvc.perform(post("/api/v1/companies/{companyId}/add-client", company.getId())
                .param("email", client.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        company = companyService.findById(company.getId());

        result.andExpect(content().json(getRequestJson(company)));
    }

    @Test
    void removeClient() throws Exception {
        Client owner = createClient(OWNER_EMAIL);
        setJwtAuth();
        Company company = companyService.create(getCompanyDto());

        Client client = createClient(CLIENT_EMAIL);
        company = companyService.addClient(company.getId(), client.getEmail());

        ResultActions result = mvc.perform(delete("/api/v1/companies/{companyId}/remove-client", company.getId())
                .param("email", client.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        company = companyService.findById(company.getId());
        result.andExpect(content().json(getRequestJson(company)));

        AssertionError error = assertThrows(AssertionError.class,
                () -> result.andExpect(jsonPath("clients[1]").value(owner)));

        assertThat(error.getMessage()).isEqualTo("No value at JSON path \"clients[1]\"");
    }

    @Test
    void getById() throws Exception {
        createClient(OWNER_EMAIL);
        setJwtAuth();
        Company company = companyService.create(getCompanyDto());

        mvc.perform(get("/api/v1/companies/{companyId}", company.getId()))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(getRequestJson(company)));
    }

    @Test
    void getByName() throws Exception {
        createClient(OWNER_EMAIL);
        setJwtAuth();
        Company company = companyService.create(getCompanyDto());

        mvc.perform(get("/api/v1/companies/name")
                .param("value", company.getName()))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(getRequestJson(company)));
    }

    @Test
    void updateDescription() throws Exception {
        createClient(OWNER_EMAIL);
        setJwtAuth();
        Company company = companyService.create(getCompanyDto());

        ResultActions result = mvc.perform(patch("/api/v1/companies/{companyId}/update-description", company.getId())
                .param("value", "new_description"))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        company = companyService.findById(company.getId());

        result.andExpect(content().json(getRequestJson(company)));
    }

    @Test
    void deleteCompany() throws Exception {
        createClient(OWNER_EMAIL);
        setJwtAuth();
        Company company = companyService.create(getCompanyDto());

        mvc.perform(delete("/api/v1/companies/{companyId}/delete", company.getId()))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Company was deleted successfully."));
    }

    private CompanyCreateDto getCompanyDto() {
        return new CompanyCreateDto("test-name", "test_description");
    }

    private String getRequestJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();

        return writer.writeValueAsString(object);
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

    private void setJwtAuth() {
        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, OWNER_EMAIL, true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
    }
}
