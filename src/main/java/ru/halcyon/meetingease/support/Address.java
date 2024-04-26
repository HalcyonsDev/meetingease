package ru.halcyon.meetingease.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class Address {
    private String region;
    private String city;
    private String street;
    private String houseNumber;
    private String displayName;
}
