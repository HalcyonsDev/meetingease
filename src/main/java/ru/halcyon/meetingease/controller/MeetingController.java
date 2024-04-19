package ru.halcyon.meetingease.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
import java.util.Map;

@RestController
@RequestMapping("api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<Meeting> create(@RequestBody @Valid MeetingCreateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, bindingResult.getAllErrors().get(0).getDefaultMessage());
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

    @PatchMapping("/{meetingId}/change-street")
    public ResponseEntity<Meeting> changeStreet(
            @PathVariable Long meetingId,
            @RequestParam
            @Size(min = 1, max = 100, message = "Street must be more than 1 character and less than 100 characters.") String value
    ) {
        Meeting meeting = meetingService.changeStreet(meetingId, value);
        return ResponseEntity.ok(meeting);
    }

    @PatchMapping("/{meetingId}/change-house")
    public ResponseEntity<Meeting> changeHouseNumber(
            @PathVariable Long meetingId,
            @RequestParam
            @Size(min = 1, max = 20, message = "House number must be more than 1 character and less than 20 characters.") String value
    ) {
        Meeting meeting = meetingService.changeHouseNumber(meetingId, value);
        return ResponseEntity.ok(meeting);
    }

    @PatchMapping("/{meetingId}/change-deal")
    public ResponseEntity<Meeting> changeDeal(
            @PathVariable Long meetingId,
            @RequestParam
            @Size(min = 1, max = 100, message = "Deal type must be more than 1 character and less than 100 characters.") String value
    ) {
        Meeting meeting = meetingService.changeDeal(meetingId, value);
        return ResponseEntity.ok(meeting);
    }

    @GetMapping("/in-waiting")
    public ResponseEntity<List<Meeting>> getAllScheduledMeetings() {
        List<Meeting> meetings = meetingService.findAllScheduledMeetings();
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/free-dates")
    public ResponseEntity<Map<Integer, List<String>>> getFreeDatesForWeek(@RequestParam String city) {
        Map<Integer, List<String>> dates = meetingService.getFreeDatesForWeek(city);
        return ResponseEntity.ok(dates);
    }
}
