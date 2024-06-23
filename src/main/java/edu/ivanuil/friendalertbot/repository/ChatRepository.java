package edu.ivanuil.friendalertbot.repository;

import edu.ivanuil.friendalertbot.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
}
