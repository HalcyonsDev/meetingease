package ru.halcyon.meetingease.service.company;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.halcyon.meetingease.dto.CompanyCreateDto;
import ru.halcyon.meetingease.exception.ResourceAlreadyExistsException;
import ru.halcyon.meetingease.exception.ResourceForbiddenException;
import ru.halcyon.meetingease.exception.ResourceNotFoundException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Company;
import ru.halcyon.meetingease.model.support.Role;
import ru.halcyon.meetingease.repository.CompanyRepository;
import ru.halcyon.meetingease.service.auth.client.ClientAuthService;
import ru.halcyon.meetingease.service.client.ClientService;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;

    private final ClientAuthService clientAuthService;
    private final ClientService clientService;

    @Override
    public Company create(CompanyCreateDto dto) {
        clientService.isVerifiedClient();
        isUniqueName(dto.getName());

        Client owner = clientService.findByEmail(clientAuthService.getAuthInfo().getEmail());

        if (owner.getCompany() != null) {
            throw new ResourceAlreadyExistsException("You already have company.");
        }

        Company company = new Company(dto.getName(), dto.getDescription());
        company = companyRepository.save(company);

        owner.setRole(Role.ADMIN);
        owner.setCompany(company);

        clientService.save(owner);

        return findById(company.getId());
    }

    @Override
    public Company addClient(Long companyId, String email) {
        Company company = findById(companyId);

        clientService.isVerifiedClient();
        isAdmin(company);

        Client client = clientService.findByEmail(email);
        if (client.getCompany() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This client can't join a company while he is in another one");
        }

        client.setCompany(company);
        client.setRole(Role.USER);
        clientService.save(client);

        return findById(companyId);
    }

    @Override
    public Company removeClient(Long companyId, String email) {
        Company company = findById(companyId);

        clientService.isVerifiedClient();
        isAdmin(company);

        Client client = clientService.findByEmail(email);
        if (!client.getCompany().equals(company)) {
            throw new ResourceForbiddenException("No access for client in this company.");
        }

        client.setCompany(null);
        clientService.save(client);

        company.getClients().remove(client);
        return companyRepository.save(company);
    }

    @Override
    public Company findById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company with this id not found."));
    }

    @Override
    public Company findByName(String name) {
        return companyRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Company with this name not found."));
    }

    @Override
    public Company updateDescription(Long companyId, String description) {
        Company company = findById(companyId);

        clientService.isVerifiedClient();
        isAdmin(company);

        company.setDescription(description);

        return companyRepository.save(company);
    }

    @Override
    public String delete(Long companyId) {
        Company company = findById(companyId);

        clientService.isVerifiedClient();
        isAdmin(company);

        for (Client client: company.getClients()) {
            client.setCompany(null);
            clientService.save(client);
        }

        companyRepository.delete(company);
        return "Company was deleted successfully.";
    }

    private void isUniqueName(String name) {
        if (companyRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Company with this name already exists.");
        }
    }

    private void isAdmin(Company company) {
        Client client = clientService.findByEmail(clientAuthService.getAuthInfo().getEmail());

        if (client.getRole().equals(Role.USER) || !company.equals(client.getCompany())) {
            throw new ResourceForbiddenException("No access for this company.");
        }
    }
}
