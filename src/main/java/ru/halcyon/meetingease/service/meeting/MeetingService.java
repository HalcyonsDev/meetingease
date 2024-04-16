package ru.halcyon.meetingease.service.meeting;

import ru.halcyon.meetingease.dto.MeetingCreateDto;
import ru.halcyon.meetingease.model.Meeting;

import java.util.List;

public interface MeetingService {
    Meeting create(MeetingCreateDto dto);
    Meeting cancel(Long meetingId);
    Meeting complete(Long meetingId);
    Meeting findById(Long meetingId);
    List<Meeting> findAllScheduledMeetings();
}
