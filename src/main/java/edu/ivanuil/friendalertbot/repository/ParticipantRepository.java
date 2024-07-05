package edu.ivanuil.friendalertbot.repository;

import edu.ivanuil.friendalertbot.entity.ParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.Set;

public interface ParticipantRepository extends JpaRepository<ParticipantEntity, String> {

    Set<ParticipantEntity> getAllByStatusNullOrUpdatedAtLessThan(Timestamp timestamp);

}
