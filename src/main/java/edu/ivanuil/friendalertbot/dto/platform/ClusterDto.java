package edu.ivanuil.friendalertbot.dto.platform;

import lombok.Data;

@Data
public class ClusterDto {

    Integer id;
    String name;
    Integer capacity;
    Integer availableCapacity;
    Integer floor;

}
