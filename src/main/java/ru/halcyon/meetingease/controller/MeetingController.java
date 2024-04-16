package ru.halcyon.meetingease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.halcyon.meetingease.dto.MeetingCreateDto;
import ru.halcyon.meetingease.model.Meeting;
import ru.halcyon.meetingease.service.meeting.MeetingService;

import java.util.List;

@RestController
@RequestMapping("api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<Meeting> create(@RequestBody @Valid MeetingCreateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        Meeting meeting = meetingService.create(dto);
        return ResponseEntity.ok(meeting);
    }

    @PostMapping("/{meetingId}/cancel")
    public ResponseEntity<Meeting> cancel(@PathVariable Long meetingId) {
        Meeting meeting = meetingService.cancel(meetingId);
        return ResponseEntity.ok(meeting);
    }

    @PostMapping("/{meetingId}/complete")
    public ResponseEntity<Meeting> complete(@PathVariable Long meetingId) {
        Meeting meeting = meetingService.complete(meetingId);
        return ResponseEntity.ok(meeting);
    }

    @GetMapping("/in-waiting")
    public ResponseEntity<List<Meeting>> getAllScheduledMeetings() {
        List<Meeting> meetings = meetingService.findAllScheduledMeetings();
        return ResponseEntity.ok(meetings);
    }
}
