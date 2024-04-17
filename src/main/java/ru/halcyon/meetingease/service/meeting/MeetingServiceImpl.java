package ru.halcyon.meetingease.service.meeting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.api.dadata.OSMNominatiumAPI;
import ru.halcyon.meetingease.dto.MeetingCreateDto;
import ru.halcyon.meetingease.exception.ResourceForbiddenException;
import ru.halcyon.meetingease.exception.ResourceNotFoundException;
import ru.halcyon.meetingease.exception.WrongDataException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Deal;
import ru.halcyon.meetingease.model.Meeting;
import ru.halcyon.meetingease.model.support.Address;
import ru.halcyon.meetingease.model.support.Role;
import ru.halcyon.meetingease.model.support.Status;
import ru.halcyon.meetingease.repository.DealRepository;
import ru.halcyon.meetingease.repository.MeetingRepository;
import ru.halcyon.meetingease.service.agent.AgentService;
import ru.halcyon.meetingease.service.auth.client.ClientAuthService;
import ru.halcyon.meetingease.service.client.ClientService;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {
    private final MeetingRepository meetingRepository;
    private final DealRepository dealRepository;

    private final ClientAuthService clientAuthService;
    private final ClientService clientService;
    private final AgentService agentService;

    private final OSMNominatiumAPI osmNominatiumAPI;

    @Override
    public Meeting create(MeetingCreateDto dto) {
        isClient();
        Client client = clientService.findByEmail(clientAuthService.getAuthInfo().getEmail());

        if (!client.getRole().equals(Role.ADMIN)) {
            throw new ResourceForbiddenException("You don't have the rights to create a meeting.");
        }

        if (findAllScheduledMeetings().size() > 10) {
            throw new WrongDataException("You have exceeded the limit for creating meetings. Maximum number of meetings: 10");
        }

        if (LocalDateTime.ofInstant(dto.getDate(), ZoneId.systemDefault()).toLocalTime().isBefore(LocalTime.of(8, 0)) ||
                LocalDateTime.ofInstant(dto.getDate(), ZoneId.systemDefault()).toLocalTime().isAfter(LocalTime.of(18, 0))) {
            throw new WrongDataException("Agents are available only from 8:00 to 18:00");
        }

        Deal deal = dealRepository.findByType(dto.getDealType())
                .orElseThrow(() -> new ResourceNotFoundException("Deal with this type not found."));
        Address address = osmNominatiumAPI.getCorrectAddress(dto.getCity(), dto.getStreet(), dto.getHouseNumber());
        String city = address.getCity().substring(1, address.getCity().length() - 1);
        Agent freeAgent = getFreeAgent(dto.getDate(), city);

        if (freeAgent == null) {
            throw new WrongDataException("Unfortunately, there are no agents available at the moment.");
        }

        Meeting meeting = Meeting.builder()
                .date(dto.getDate())
                .address(address.getDisplayName())
                .agent(freeAgent)
                .city(city)
                .deal(deal)
                .clients(List.of(client))
                .status(Status.IN_WAITING)
                .build();

        return meetingRepository.save(meeting);
    }

    @Override
    public Meeting cancel(Long meetingId) {
        Meeting meeting = findById(meetingId);
        meeting.setStatus(Status.CANCELLED);

        return meetingRepository.save(meeting);
    }

    @Override
    public Meeting complete(Long meetingId) {
        Meeting meeting = findById(meetingId);
        meeting.setStatus(Status.COMPLETED);

        return meetingRepository.save(meeting);
    }

    @Override
    public Meeting findById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting with this id not found."));
    }

    @Override
    public List<Meeting> findAllScheduledMeetings() {
        isClient();
        Client client = clientService.findByEmail(clientAuthService.getAuthInfo().getEmail());

        return meetingRepository.findAllByStatusAndClientsContaining(Status.IN_WAITING, client);
    }

    @Override
    public Map<Integer, List<String>> getFreeDatesForWeek(String city) {
        Map<Integer, List<String>> dates = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour(), minute = now.getMinute() < 30 ? 0 : 30;

        boolean isFreeToday = false;
        if (8 < hour && hour < 18) {
            isFreeToday = true;
            List<String> today = new ArrayList<>();

            for (int h = hour; h <= 18; h++) {
                for (int m = minute; m <= 30; m += 30) {
                    today.add(getTimeInStringFormat(h, m));
                }
            }

            dates.put(now.getDayOfMonth(), today);
        }

        for (int d = now.getDayOfMonth() + 1; d <= now.getDayOfMonth() + (isFreeToday ? 6 : 7); d++) {
            List<String> day = new ArrayList<>();

            for (int h = 8; h <= 18; h++) {
                for (int m = 0; m <= 30; m += 30) {
                    if (h == 18 && m == 30) continue;
                    day.add(getTimeInStringFormat(h, m));
                }
            }

            dates.put(d, day);
        }

        List<Meeting> meetings = meetingRepository.findAllByCityAndStatus(city, Status.IN_WAITING);
        for (Meeting meeting: meetings) {
            LocalDateTime date = LocalDateTime.ofInstant(meeting.getDate(), ZoneId.systemDefault());
            int day = date.getDayOfMonth();
            String time = getTimeInStringFormat(date.getHour(), date.getMinute());

            List<String> meetingDates = dates.get(day);
            System.out.println(day + " " + time + " " + meetingDates);

            if (meetingDates.contains(time)) {
                meetingDates.remove(time);
                dates.replace(day, meetingDates);
            }
        }

        return dates;
    }

    private String getTimeInStringFormat(int h, int m) {
        return h + ":" + (m < 10 ? "0" + m : m);
    }

    private void isClient() {
        boolean isClient = clientAuthService.getAuthInfo().isClient();

        if (!isClient) {
            throw new ResourceForbiddenException("This feature is not allowed for agents");
        }
    }

    private Agent getFreeAgent(Instant date, String city) {
        System.out.println(city);
        List<Agent> agents = agentService.findAllByCity(city);
        System.out.println(agents);

        for (Agent agent: agents) {
            boolean isFree = true;

             for (Meeting meeting: agent.getMeetings()) {
                 if (date.compareTo(meeting.getDate()) >= 0 &&
                        date.compareTo(meeting.getDate().plus(Duration.ofHours(1))) <= 0) {
                     isFree = false;
                     break;
                }
             }

             if (!isFree) {
                 continue;
             }

             return agent;
        }

        return null;
    }
}
