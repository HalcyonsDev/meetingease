package ru.halcyon.meetingease.service.meeting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.api.osm.OSMNominatiumAPI;
import ru.halcyon.meetingease.dto.MeetingCreateDto;
import ru.halcyon.meetingease.exception.ResourceForbiddenException;
import ru.halcyon.meetingease.exception.ResourceNotFoundException;
import ru.halcyon.meetingease.exception.InvalidCredentialsException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Deal;
import ru.halcyon.meetingease.model.Meeting;
import ru.halcyon.meetingease.security.AuthenticatedDataProvider;
import ru.halcyon.meetingease.service.agent.AgentService;
import ru.halcyon.meetingease.service.auth.ClientAuthService;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.support.Address;
import ru.halcyon.meetingease.support.Role;
import ru.halcyon.meetingease.support.Status;
import ru.halcyon.meetingease.repository.DealRepository;
import ru.halcyon.meetingease.repository.MeetingRepository;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final DealRepository dealRepository;

    private final ClientAuthService clientAuthService;
    private final ClientService clientService;
    private final AgentService agentService;
    private final AuthenticatedDataProvider authenticatedDataProvider;

    private final OSMNominatiumAPI osmNominatiumAPI;

    public Meeting create(MeetingCreateDto dto) {
        clientService.isVerifiedClient();
        Client client = clientService.findByEmail(authenticatedDataProvider.getEmail());

        if (client.getRole() == Role.USER) {
            throw new ResourceForbiddenException("You don't have the rights to create a meeting.");
        }

        if (findAllScheduledMeetings().size() > 10) {
            throw new InvalidCredentialsException("You have exceeded the limit for creating meetings. Maximum number of meetings: 10");
        }

        if (LocalDateTime.ofInstant(dto.getDate(), ZoneId.systemDefault()).toLocalTime().isBefore(LocalTime.of(8, 0)) ||
                LocalDateTime.ofInstant(dto.getDate(), ZoneId.systemDefault()).toLocalTime().isAfter(LocalTime.of(18, 0))) {
            throw new InvalidCredentialsException("Agents are available only from 8:00 to 18:00");
        }

        Deal deal = findDealByType(dto.getDealType());
        Address address = osmNominatiumAPI.getCorrectAddress(dto.getCity(), dto.getStreet(), dto.getHouseNumber());
        Optional<Agent> freeAgent = getFreeAgent(dto.getDate(), address.getCity());

        if (freeAgent.isEmpty()) {
            throw new InvalidCredentialsException("Unfortunately, there are no agents available at the moment.");
        }

        Meeting meeting = Meeting.builder()
                .date(dto.getDate())
                .address(address.getDisplayName())
                .agent(freeAgent.get())
                .city(address.getCity())
                .street(address.getStreet())
                .houseNumber(address.getHouseNumber())
                .deal(deal)
                .clients(List.of(client))
                .status(Status.IN_WAITING)
                .build();

        return meetingRepository.save(meeting);
    }

    public Meeting cancel(Long meetingId) {
        Meeting meeting = findById(meetingId);
        isCompanyAdmin(meeting);
        meeting.setStatus(Status.CANCELLED);

        return meetingRepository.save(meeting);
    }

    public Meeting complete(Long meetingId) {
        Meeting meeting = findById(meetingId);
        isCompanyAdmin(meeting);
        meeting.setStatus(Status.COMPLETED);

        return meetingRepository.save(meeting);
    }

    public Meeting changeStreet(Long meetingId, String street) {
        clientService.isVerifiedClient();
        Meeting meeting = findById(meetingId);
        isCompanyAdmin(meeting);

        Address address = osmNominatiumAPI.getCorrectAddress(meeting.getCity(), street, meeting.getHouseNumber());

        meeting.setAddress(address.getDisplayName());
        meeting.setStreet(address.getStreet());

        return meetingRepository.save(meeting);
    }

    public Meeting changeHouseNumber(Long meetingId, String houseNumber) {
        clientService.isVerifiedClient();
        Meeting meeting = findById(meetingId);
        isCompanyAdmin(meeting);

        Address address = osmNominatiumAPI.getCorrectAddress(meeting.getCity(), meeting.getStreet(), houseNumber);

        meeting.setAddress(address.getDisplayName());
        meeting.setHouseNumber(address.getHouseNumber());

        return meetingRepository.save(meeting);
    }

    public Meeting changeDeal(Long meetingId, String dealType) {
        clientService.isVerifiedClient();
        Meeting meeting = findById(meetingId);
        isCompanyAdmin(meeting);

        Deal deal = findDealByType(dealType);
        meeting.setDeal(deal);

        return meetingRepository.save(meeting);
    }

    public Meeting findById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting with this id not found."));
    }

    public List<Meeting> findAllScheduledMeetings() {
        clientService.isVerifiedClient();
        Client client = clientService.findByEmail(authenticatedDataProvider.getEmail());

        return meetingRepository.findAllByStatusAndClientsContaining(Status.IN_WAITING, client);
    }
    
    public Map<Integer, List<String>> getFreeDatesForWeek(String city) {
        LocalDateTime now = LocalDateTime.now();
        Map<Integer, List<String>> dates = getDates(now);

        List<Meeting> meetings = meetingRepository.findAllByCityAndStatus(city, Status.IN_WAITING);
        for (Meeting meeting: meetings) {
            LocalDateTime date = LocalDateTime.ofInstant(meeting.getDate(), ZoneId.systemDefault());
            int day = date.getDayOfMonth();
            String time = getTimeInStringFormat(date.getHour(), date.getMinute());

            List<String> meetingDates = dates.get(day);

            if (meetingDates.contains(time)) {
                meetingDates.remove(time);
                dates.replace(day, meetingDates);
            }
        }

        return dates;
    }

    private boolean isFreeToday(int hour) {
        return 8 < hour && hour < 18;
    }

    private Map<Integer, List<String>> getDates(LocalDateTime now) {
        int hour = now.getHour();
        int minute = now.getMinute() < 30 ? 0 : 30;
        Map<Integer, List<String>> dates = new HashMap<>();

        if (8 < hour && hour < 18) {
            List<String> today = new ArrayList<>();

            for (int h = hour; h <= 18; h++) {
                for (int m = minute; m <= 30; m += 30) {
                    today.add(getTimeInStringFormat(h, m));
                }
            }

            dates.put(now.getDayOfMonth(), today);
        }

        return setTimeForDates(now, dates);
    }

    private Map<Integer, List<String>> setTimeForDates(LocalDateTime now, Map<Integer, List<String>> dates) {
        for (int d = now.getDayOfMonth() + 1; d <= now.getDayOfMonth() + (isFreeToday(now.getHour()) ? 6 : 7); d++) {
            List<String> day = new ArrayList<>();

            for (int h = 8; h <= 18; h++) {
                for (int m = 0; m <= 30; m += 30) {
                    if (h == 18 && m == 30) continue;
                    day.add(getTimeInStringFormat(h, m));
                }
            }

            dates.put(d, day);
        }

        return dates;
    }

    public Boolean existsByAgentAndClient(Agent agent, Client client) {
        return meetingRepository.existsByAgentAndClientsContainingAndStatus(agent, client, Status.IN_WAITING);
    }

    private String getTimeInStringFormat(int h, int m) {
        return h + ":" + (m < 10 ? "0" + m : m);
    }

    private Optional<Agent> getFreeAgent(Instant date, String city) {
        List<Agent> agents = agentService.findAllByCity(city);

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

             return Optional.of(agent);
        }

        return Optional.empty();
    }

    private void isCompanyAdmin(Meeting meeting) {
        Client client = clientService.findByEmail(authenticatedDataProvider.getEmail());

        if (client.getRole() == Role.USER || !meeting.getClients().get(0).getCompany().equals(client.getCompany())) {
            throw new ResourceForbiddenException("You don't have the rights to change this meeting.");
        }
    }

    private Deal findDealByType(String dealType) {
        return dealRepository.findByType(dealType)
                .orElseThrow(() -> new ResourceNotFoundException("Deal with this type not found."));
    }
}
