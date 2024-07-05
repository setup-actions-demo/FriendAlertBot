package edu.ivanuil.friendalertbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "participant")
public class ParticipantEntity {

    @Id
    @Column(name = "login", nullable = false, length = Integer.MAX_VALUE)
    private String login;

    @Column(name = "class_name", length = Integer.MAX_VALUE)
    private String className;

    @Column(name = "parallel_name", length = Integer.MAX_VALUE)
    private String parallelName;

    @Column(name = "exp_value")
    private Integer expValue;

    @Column(name = "level")
    private Integer level;

    @Column(name = "exp_to_next_level")
    private Integer expToNextLevel;

    @Column(name = "campus", length = Integer.MAX_VALUE)
    private String campus;

    @Column(name = "status", length = Integer.MAX_VALUE)
    private String status;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Timestamp updatedAt;

}