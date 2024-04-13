package ru.halcyon.meetingease.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "required_documents")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> requiredDocuments;

    @OneToMany(mappedBy = "deal")
    @JsonBackReference
    private List<Meeting> meetings;
}
