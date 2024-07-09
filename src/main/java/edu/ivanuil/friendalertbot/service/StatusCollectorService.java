package edu.ivanuil.friendalertbot.service;

import edu.ivanuil.friendalertbot.dto.status.ParticipantServiceStatusDto;
import edu.ivanuil.friendalertbot.dto.status.School21PlatformBindingStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusCollectorService {

    private final ParticipantService participantService;
    private final School21PlatformBinding platformBinding;

    public ParticipantServiceStatusDto getParticipantServiceStatus() {
        var res = new ParticipantServiceStatusDto();

        res.setPageSize(participantService.getPageSize());
        res.setRefreshInterval(participantService.getRefreshInterval());
        res.setParticipantBatchMaxSize(participantService.getParticipantBatchMaxSize());
        res.setBatchCurrentSize(participantService.getParticipantBatch().size());
        res.setUserListSizeInThisOrLastIteration(participantService.getParticipantListSizeInThisIteration());
        res.setWorking(participantService.isWorking());

        return res;
    }

    public School21PlatformBindingStatusDto getPlatformBindingStatus() {
        var res = new School21PlatformBindingStatusDto();

        res.setUsername(platformBinding.getUsername());
        res.setRequestRate(platformBinding.getRequestRatePerSecond());

        return res;
    }

}
