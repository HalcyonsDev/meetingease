package ru.halcyon.meetingease.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import ru.halcyon.meetingease.model.support.Role;

import java.util.List;

@Entity
@Table(name = "clients")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client extends BaseModel {
    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "position")
    private String position;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "password")
    private String password;

    @Column(name = "photo")
    private String photo;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    @JsonManagedReference
    private Company company;

    @ManyToMany(mappedBy = "clients")
    @JsonBackReference
    private List<Meeting> meetings;
}
