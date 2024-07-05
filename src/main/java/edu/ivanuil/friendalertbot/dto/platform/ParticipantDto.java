package edu.ivanuil.friendalertbot.dto.platform;

import lombok.Data;

@Data
public class ParticipantDto {

    private String login;
    private String className;
    private String parallelName;
    private Integer expValue;
    private Integer level;
    private Integer expToNextLevel;
    private CampusDto campus;
    private String status;

}
