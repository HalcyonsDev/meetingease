package ru.halcyon.meetingease.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Meeting extends BaseModel {
    @Column(name = "date")
    private Instant date;

    @Column(name = "address")
    private String address;

    @ManyToOne
    @JoinColumn(name = "agent_id", referencedColumnName = "Id")
    @JsonManagedReference
    private Agent agent;

    @ManyToOne
    @JoinColumn(name = "deal_id", referencedColumnName = "id")
    @JsonManagedReference
    private Deal deal;

    @ManyToMany
    @JoinTable(
            name = "meetings_clients",
            joinColumns = @JoinColumn(name = "meeting_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "client_id", referencedColumnName = "id")
    )
    @JsonManagedReference
    private List<Client> clients;
}
