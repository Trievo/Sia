package com.trievosoftware.application.domain;


import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A TempMutes.
 */
@Entity
@Table(name = "temp_mutes")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TempMutes implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "finish", nullable = false)
    private Instant finish;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGuildId() {
        return guildId;
    }

    public TempMutes guildId(Long guildId) {
        this.guildId = guildId;
        return this;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public Long getUserId() {
        return userId;
    }

    public TempMutes userId(Long userId) {
        this.userId = userId;
        return this;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Instant getFinish() {
        return finish;
    }

    public TempMutes finish(Instant finish) {
        this.finish = finish;
        return this;
    }

    public void setFinish(Instant finish) {
        this.finish = finish;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TempMutes tempMutes = (TempMutes) o;
        if (tempMutes.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), tempMutes.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TempMutes{" +
            "id=" + getId() +
            ", guildId=" + getGuildId() +
            ", userId=" + getUserId() +
            ", finish='" + getFinish() + "'" +
            "}";
    }
}
