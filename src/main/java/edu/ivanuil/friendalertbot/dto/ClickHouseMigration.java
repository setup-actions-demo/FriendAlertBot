package edu.ivanuil.friendalertbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@AllArgsConstructor
public class ClickHouseMigration {
    private String name;
    private String filename;
    private LocalDateTime executedAt;
    private int order;
    private String request;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClickHouseMigration that)) return false;
        return Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public boolean assertEquals(ClickHouseMigration migration) {
        return
                this.name.equals(migration.getName())
                        && this.getFilename().equals(migration.getFilename())
                        && this.getRequest().equals(migration.getRequest());
    }

}
