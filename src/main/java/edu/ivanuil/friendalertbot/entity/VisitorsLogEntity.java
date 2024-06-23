package edu.ivanuil.friendalertbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "visitors_log")
@NoArgsConstructor
@AllArgsConstructor
public class VisitorsLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visitors_log_id_gen")
    @SequenceGenerator(name = "visitors_log_id_gen", sequenceName = "visitor_log_sequence", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "\"timestamp\"")
    @CreationTimestamp
    private Timestamp timestamp;

    @Column(name = "campus", length = Integer.MAX_VALUE)
    private String campus;

    @Column(name = "cluster", length = Integer.MAX_VALUE)
    private String cluster;

    @Column(name = "visitors_count")
    private Integer visitorsCount;

}