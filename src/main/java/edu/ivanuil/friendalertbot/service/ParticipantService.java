package edu.ivanuil.friendalertbot.service;

import edu.ivanuil.friendalertbot.entity.CampusEntity;
import edu.ivanuil.friendalertbot.entity.ParticipantEntity;
import edu.ivanuil.friendalertbot.exception.EntityNotFoundException;
import edu.ivanuil.friendalertbot.mapper.ParticipantMapper;
import edu.ivanuil.friendalertbot.repository.CampusRepository;
import edu.ivanuil.friendalertbot.repository.ParticipantInfoLogRepository;
import edu.ivanuil.friendalertbot.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static edu.ivanuil.friendalertbot.util.TimeFormatUtils.formatIntervalFromNow;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipantService {

    private final School21PlatformBinding platformBinding;
    private final ParticipantMapper participantMapper;

    private final CampusRepository campusRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantInfoLogRepository participantLogRepository;

    @Value("${s21.participants.page-size}")
    private int pageSize;
    @Value("${s21.participants.obsolescence-time}")
    private long refreshInterval;

    public void refreshAll() {
        campusRepository.findAll().forEach(this::refreshForCampus);
    }

    public void refreshForCampus(final CampusEntity campus) {
        int start = 0;
        int count = Integer.MAX_VALUE;
        while (count != 0) {
            var participants = platformBinding.getParticipantList(campus.getId(), pageSize, start).getParticipants();
            start += participants.length;
            count = participants.length;
            log.info("Retrieved {} participants for campus '{}'", count, campus.getName());

            for (var participant : participants) {
                if (!participantRepository.existsById(participant)) {
                    var entity = new ParticipantEntity();
                    entity.setLogin(participant);
                    participantRepository.save(entity);
                }
            }

            getParticipantsInfo();
        }
    }

    public void getParticipantsInfo() {
        var participants = participantRepository.getAllByStatusNullOrUpdatedAtLessThan(
                new Timestamp(System.currentTimeMillis() - refreshInterval));
        if (participants.isEmpty())
            return;
        log.info("{} participants have no user info or require refreshing", participants.size());

        Timestamp operationsStart = new Timestamp(System.currentTimeMillis());
        for (var participant : participants) {
            try {
                var participantDto = platformBinding.getUserInfo(participant.getLogin());
                var participantEntity = participantMapper.toParticipantEntity(participantDto);
                participantRepository.save(participantEntity);
                participantLogRepository.appendParticipantInfoLog(participantEntity);
            } catch (EntityNotFoundException e) {
                log.warn("Participant {} not found", participant.getLogin());
                participantRepository.deleteById(participant.getLogin());
            }
        }
        log.info("Retrieved info for {} participants in {}", participants.size(),
                formatIntervalFromNow(operationsStart));
    }

}
