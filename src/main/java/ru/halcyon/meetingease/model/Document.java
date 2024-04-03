package ru.halcyon.meetingease.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document extends BaseModel {
    @Column(name = "name")
    private String name;

    @Column(name = "is_ready")
    private Boolean isReady;

    @ManyToOne
    @JoinColumn(name = "deal_id", referencedColumnName = "id")
    @JsonManagedReference
    private Deal deal;
}
