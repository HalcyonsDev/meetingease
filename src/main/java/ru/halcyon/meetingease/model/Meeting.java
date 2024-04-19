package ru.halcyon.meetingease.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import ru.halcyon.meetingease.model.support.Status;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "meetings")
@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Meeting extends BaseModel {
    @Column(name = "date")
    private Instant date;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "house_number")
    private String houseNumber;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_id", referencedColumnName = "Id")
    @JsonManagedReference
    private Agent agent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deal_id", referencedColumnName = "id")
    @JsonManagedReference
    private Deal deal;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "meetings_clients",
            joinColumns = @JoinColumn(name = "meeting_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "client_id", referencedColumnName = "id")
    )
    @JsonManagedReference
    private List<Client> clients;
}
