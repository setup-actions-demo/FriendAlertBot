package edu.ivanuil.friendalertbot.repository;

import edu.ivanuil.friendalertbot.entity.CampusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CampusRepository extends JpaRepository<CampusEntity, UUID> {

}
