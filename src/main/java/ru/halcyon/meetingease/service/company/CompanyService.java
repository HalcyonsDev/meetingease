package ru.halcyon.meetingease.service.company;

import ru.halcyon.meetingease.dto.CompanyCreateDto;
import ru.halcyon.meetingease.model.Company;

public interface CompanyService {
    Company create(CompanyCreateDto dto);
    Company addClient(Long companyId, String email);
    Company removeClient(Long companyId, String email);
    Company findById(Long companyId);
    Company findByName(String name);
    Company updateDescription(Long companyId, String description);
    String delete(Long companyId);
}
