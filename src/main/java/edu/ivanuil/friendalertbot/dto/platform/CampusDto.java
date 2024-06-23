package edu.ivanuil.friendalertbot.dto.platform;

import lombok.Data;

import java.util.UUID;

@Data
public class CampusDto {

    private UUID id;
    private String shortName;
    private String fullName;

}
