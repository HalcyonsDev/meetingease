package ru.halcyon.meetingease.service.meeting;

import ru.halcyon.meetingease.dto.MeetingCreateDto;
import ru.halcyon.meetingease.model.Meeting;

import java.util.List;
import java.util.Map;

public interface MeetingService {
    Meeting create(MeetingCreateDto dto);
    Meeting cancel(Long meetingId);
    Meeting complete(Long meetingId);
    Meeting findById(Long meetingId);
    List<Meeting> findAllScheduledMeetings();
    Map<Integer, List<String>> getFreeDatesForWeek(String city);
}
