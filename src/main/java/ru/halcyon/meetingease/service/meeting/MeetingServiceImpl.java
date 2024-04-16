package ru.halcyon.meetingease.service.meeting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.api.dadata.DadataAPI;
import ru.halcyon.meetingease.dto.MeetingCreateDto;
import ru.halcyon.meetingease.exception.ResourceForbiddenException;
import ru.halcyon.meetingease.exception.ResourceNotFoundException;
import ru.halcyon.meetingease.exception.WrongDataException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Deal;
import ru.halcyon.meetingease.model.Meeting;
import ru.halcyon.meetingease.model.support.Role;
import ru.halcyon.meetingease.model.support.Status;
import ru.halcyon.meetingease.repository.DealRepository;
import ru.halcyon.meetingease.repository.MeetingRepository;
import ru.halcyon.meetingease.service.agent.AgentService;
import ru.halcyon.meetingease.service.auth.client.ClientAuthService;
import ru.halcyon.meetingease.service.client.ClientService;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {
    private final MeetingRepository meetingRepository;
    private final DealRepository dealRepository;

    private final ClientAuthService clientAuthService;
    private final ClientService clientService;
    private final AgentService agentService;

    private final DadataAPI dadataAPI;

    @Override
    public Meeting create(MeetingCreateDto dto) {
        isClient();
        Client client = clientService.findByEmail(clientAuthService.getAuthInfo().getEmail());

        if (!client.getRole().equals(Role.ADMIN)) {
            throw new ResourceForbiddenException("You don't have the rights to create a meeting.");
        }

        if (LocalDateTime.ofInstant(dto.getDate(), ZoneId.systemDefault()).toLocalTime().isBefore(LocalTime.of(8, 0)) ||
                LocalDateTime.ofInstant(dto.getDate(), ZoneId.systemDefault()).toLocalTime().isAfter(LocalTime.of(18, 0))) {
            throw new WrongDataException("Agents are available only from 8:00 to 18:00");
        }

        Deal deal = dealRepository.findByType(dto.getDealType())
                .orElseThrow(() -> new ResourceNotFoundException("Deal with this type not found."));
        String address = dadataAPI.getCorrectAddress(dto.getAddress());
        String city = address.split(" ")[1];
        Agent freeAgent = getFreeAgent(dto.getDate(), city.substring(0, city.length() - 1));

        if (freeAgent == null) {
            throw new WrongDataException("Unfortunately, there are no agents available at the moment.");
        }

        Meeting meeting = Meeting.builder()
                .date(dto.getDate())
                .address(address)
                .agent(freeAgent)
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

    private void isClient() {
        boolean isClient = clientAuthService.getAuthInfo().isClient();

        if (!isClient) {
            throw new ResourceForbiddenException("This feature is not allowed for agents");
        }
    }

    private Agent getFreeAgent(Instant date, String city) {
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

             return agent;
        }

        return null;
    }
}
