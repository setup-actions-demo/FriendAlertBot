package edu.ivanuil.friendalertbot.dto.platform;

import lombok.Data;

@Data
public class ClusterDto {

    private Integer id;
    private String name;
    private Integer capacity;
    private Integer availableCapacity;
    private Integer floor;

}
