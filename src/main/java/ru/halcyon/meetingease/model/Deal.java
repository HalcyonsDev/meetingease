package ru.halcyon.meetingease.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "deals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Deal extends BaseModel {
    @Column(name = "type")
    private String type;

    @OneToMany(mappedBy = "deal")
    @JsonBackReference
    private List<Document> requiredDocuments;

    @OneToMany(mappedBy = "deal")
    @JsonBackReference
    private List<Meeting> meetings;
}
