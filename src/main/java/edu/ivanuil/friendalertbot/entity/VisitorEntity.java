package edu.ivanuil.friendalertbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "visitors")
public class VisitorEntity {

    @Id
    @Column(name = "username", nullable = false, length = Integer.MAX_VALUE)
    private String login;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cluster")
    private ClusterEntity cluster;

    @Column(name = "\"row\"", length = 1)
    private String row;

    @Column(name = "number")
    private Integer number;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof VisitorEntity visitor)) return false;
        return Objects.equals(login, visitor.login);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(login);
    }

    @Override
    public String toString() {
        return login;
    }

}
