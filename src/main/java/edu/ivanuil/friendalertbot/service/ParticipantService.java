package edu.ivanuil.friendalertbot.service;

import edu.ivanuil.friendalertbot.entity.CampusEntity;
import edu.ivanuil.friendalertbot.entity.ParticipantEntity;
import edu.ivanuil.friendalertbot.exception.EntityNotFoundException;
import edu.ivanuil.friendalertbot.mapper.ParticipantMapper;
import edu.ivanuil.friendalertbot.repository.CampusRepository;
import edu.ivanuil.friendalertbot.repository.ParticipantInfoLogRepository;
import edu.ivanuil.friendalertbot.repository.ParticipantRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static edu.ivanuil.friendalertbot.util.TimeFormatUtils.formatIntervalFromNow;

@Service
@Slf4j
@Getter
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
    @Value("${s21.participants.batch-max-size}")
    private int participantBatchMaxSize = 1000;

    private boolean isWorking = false;
    private final Set<ParticipantEntity> participantBatch = new HashSet<>();
    private int participantListSizeInThisIteration = 0;

    public void refreshAll() {
        participantListSizeInThisIteration = 0;
        isWorking = true;
        campusRepository.findAll().forEach(this::refreshForCampus);
        isWorking = false;
    }

    public void refreshForCampus(final CampusEntity campus) {
        int start = 0;
        int count = Integer.MAX_VALUE;
        while (count != 0) {
            var participants = platformBinding.getParticipantList(campus.getId(), pageSize, start).getParticipants();
            start += participants.length;
            count = participants.length;
            participantListSizeInThisIteration += participants.length;
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
                participantBatch.add(participantEntity);
            } catch (EntityNotFoundException e) {
                log.warn("Participant {} not found", participant.getLogin());
                participantRepository.deleteById(participant.getLogin());
            }
            if (participantBatch.size() >= participantBatchMaxSize) {
                saveBatch(participantBatch);
            }
        }
        saveBatch(participantBatch);
        log.info("Retrieved info for {} participants in {}", participants.size(),
                formatIntervalFromNow(operationsStart));
    }

    private void saveBatch(Collection<ParticipantEntity> participants) {
        if (participants.isEmpty())
            return;

        participants.forEach(entity -> entity.setUpdatedAt(new Timestamp(System.currentTimeMillis())));
        participantRepository.saveAll(participants);
        participantLogRepository.appendParticipantInfoLog(participants);
        log.info("Saved participant info batch ({} rows)", participants.size());
        participants.clear();
    }

}
