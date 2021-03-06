package com.trievosoftware.application.service;

import com.trievosoftware.application.domain.Level;
import com.trievosoftware.application.domain.Premium;
import com.trievosoftware.application.domain.PremiumInfo;
import com.trievosoftware.application.exceptions.NoPremiumFoundException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing Premium.
 */
public interface PremiumService {

    /**
     * Save a premium.
     *
     * @param premium the entity to save
     * @return the persisted entity
     */
    Premium save(Premium premium);

    /**
     * Get all the premiums.
     *
     * @return the list of entities
     */
    List<Premium> findAll();


    /**
     * Get the "id" premium.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Optional<Premium> findOne(Long id);

    /**
     * Delete the "id" premium.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    Optional<Premium> findByDiscordId(Long discordId);

    List<Premium> findAllByUntilIsLessThan(Instant now);

    PremiumInfo getPremiumInfo(Guild guild);

    PremiumInfo getPremiumInfo(User user);

    void addPremiumForever(Guild guild, Level level);

    void addPremiumForever(User user, Level level);

    void addPremium(Guild guild, Level level, int time, TemporalUnit unit);

    void addPremium(User user, Level level, int time, TemporalUnit unit);

    void cancelPremium(Guild guild) throws NoPremiumFoundException;

    void cancelPremium(User user) throws NoPremiumFoundException;

    List<Long> cleanPremiumList();
}
