package ru.halcyon.meetingease.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.halcyon.meetingease.dto.CompanyCreateDto;
import ru.halcyon.meetingease.model.Company;
import ru.halcyon.meetingease.service.company.CompanyService;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<Company> create(@RequestBody @Valid CompanyCreateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        Company company = companyService.create(dto);
        return ResponseEntity.ok(company);
    }

    @PostMapping("/{companyId}/add-client")
    public ResponseEntity<Company> addClient(@PathVariable Long companyId, @RequestParam String email) {
        Company company = companyService.addClient(companyId, email);
        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{companyId}/remove-client")
    public ResponseEntity<Company> removeClient(@PathVariable Long companyId, @RequestParam String email) {
        Company company = companyService.removeClient(companyId, email);
        return ResponseEntity.ok(company);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<Company> getById(@PathVariable Long companyId) {
        Company company = companyService.findById(companyId);
        return ResponseEntity.ok(company);
    }

    @GetMapping("/name")
    public ResponseEntity<Company> getByName(@RequestParam String value) {
        Company company = companyService.findByName(value);
        return ResponseEntity.ok(company);
    }

    @PatchMapping("/{companyId}/update-description")
    public ResponseEntity<Company> updateDescription(
            @PathVariable Long companyId,
            @RequestParam
            @Size(min = 1, max = 500, message = "Description must be more than 1 character and less than 500 characters.") String value
    ) {
        Company company = companyService.updateDescription(companyId, value);
        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{companyId}/delete")
    public ResponseEntity<String> delete(@PathVariable Long companyId) {
        String response = companyService.delete(companyId);
        return ResponseEntity.ok(response);
    }
}
