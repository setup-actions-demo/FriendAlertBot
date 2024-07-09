package edu.ivanuil.friendalertbot.web.controller;

import edu.ivanuil.friendalertbot.dto.status.StatusAggregateDto;
import edu.ivanuil.friendalertbot.service.StatusCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ServiceStatusController {

    private final StatusCollectorService statusService;

    @GetMapping()
    public StatusAggregateDto getStatuses() {
        var res = new StatusAggregateDto();

        res.setStatuses(new Object[] {
                statusService.getParticipantServiceStatus(),
                statusService.getPlatformBindingStatus()
        });

        return res;
    }

}
