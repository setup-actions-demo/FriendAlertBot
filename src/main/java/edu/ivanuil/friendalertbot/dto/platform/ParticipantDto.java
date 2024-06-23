package edu.ivanuil.friendalertbot.dto.platform;

import lombok.Data;

@Data
public class ParticipantDto {

    String login;
    String className;
    String parallelName;
    Integer expValue;
    Integer level;
    Integer expToNextLevel;
    CampusDto campus;
    String status;

}
