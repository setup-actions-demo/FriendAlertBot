package edu.ivanuil.friendalertbot.dto.status;

import lombok.Data;

@Data
public class ParticipantServiceStatusDto {

    private int pageSize;
    private long refreshInterval;
    private int participantBatchMaxSize;

    private boolean isWorking;
    private int batchCurrentSize;
    private int userListSizeInThisOrLastIteration;

}
