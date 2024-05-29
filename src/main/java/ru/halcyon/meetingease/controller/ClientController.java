package ru.halcyon.meetingease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.halcyon.meetingease.dto.ClientUpdateDto;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.support.Role;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping("/upload-photo")
    public ResponseEntity<Client> uploadPhoto(@RequestParam("file") MultipartFile file) {
        Client client = clientService.uploadPhoto(file);
        return ResponseEntity.ok(client);
    }

    @PatchMapping("/update-data")
    public ResponseEntity<Client> updateData(@RequestBody @Valid ClientUpdateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        Client client = clientService.updateData(dto);
        return ResponseEntity.ok(client);
    }

    @PatchMapping("/{clientId}/update-role")
    public ResponseEntity<Client> updateRole(@PathVariable Long clientId, @RequestParam("role") Role newRole) {
        Client client = clientService.updateRole(clientId, newRole);
        return ResponseEntity.ok(client);
    }
}
