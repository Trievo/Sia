package com.trievosoftware.application.service.impl;

import com.trievosoftware.application.domain.Level;
import com.trievosoftware.application.domain.PremiumInfo;
import com.trievosoftware.application.exceptions.NoPremiumFoundException;
import com.trievosoftware.application.service.PremiumService;
import com.trievosoftware.application.domain.Premium;
import com.trievosoftware.application.repository.PremiumRepository;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing Premium.
 */
@SuppressWarnings("Duplicates")
@Service
@Transactional
public class PremiumServiceImpl implements PremiumService {

    private final Logger log = LoggerFactory.getLogger(PremiumServiceImpl.class);

    private final PremiumRepository premiumRepository;

    public PremiumServiceImpl(PremiumRepository premiumRepository) {
        this.premiumRepository = premiumRepository;
    }

    /**
     * Save a premium.
     *
     * @param premium the entity to save
     * @return the persisted entity
     */
    @Override
    public Premium save(Premium premium) {
        log.debug("Request to save Premium : {}", premium);
        return premiumRepository.save(premium);
    }

    /**
     * Get all the premiums.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Premium> findAll() {
        log.debug("Request to get all Premiums");
        return premiumRepository.findAll();
    }


    /**
     * Get one premium by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Premium> findOne(Long id) {
        log.debug("Request to get Premium : {}", id);
        return premiumRepository.findById(id);
    }

    /**
     * Delete the premium by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Premium : {}", id);
        premiumRepository.deleteById(id);
    }

    /**
     * Get one premium by id
     * @param discordId the id of the GUild or User
     * @return
     */
    @Override
    public Optional<Premium> findByDiscordId(Long discordId) {
        log.debug("Request to get Premium for : {}", discordId);
        return premiumRepository.findByDiscordId(discordId);
    }

    /**
     * Finds all Premium memberships before a particular time
     *
     * @param now time
     * @return List of Premium guilds
     */
    @Override
    public List<Premium> findAllByUntilIsLessThan(Instant now) {
        log.debug("Request to get all Premiums Before Current Time={}", now);
        return premiumRepository.findAllByUntilIsLessThan(now);
    }

    /**
     * Get the premium info of a guild. If they dont have anything registered, give them a blank one.
     *
     * @param guild the Guild of the entity
     * @return PremiumInfo
     */
    @Override
    public PremiumInfo getPremiumInfo(Guild guild) {
        log.debug("Request to get PremiumInfo for Guild : {}", guild.getName());
        Optional<Premium> premiumOptional = findByDiscordId(guild.getIdLong());
        return premiumOptional.map(premium ->
            new PremiumInfo(premium.getLevel(),
                premium.getUntil())).orElseGet(PremiumInfo::new);
    }

    /**
     * Get the premium info of a User. If they dont have anything registered, give them a blank one.
     *
     * @param user the User of the entity
     * @return PremiumInfo
     */
    @Override
    public PremiumInfo getPremiumInfo(User user) {
        log.debug("Request to get PremiumInfo for User : {}", user.getName());
        Optional<Premium> premiumOptional = findByDiscordId(user.getIdLong());
        return premiumOptional.map(premium ->
            new PremiumInfo(premium.getLevel(),
                premium.getUntil())).orElseGet(PremiumInfo::new);
    }

    /**
     * Adds premium forever for a guild
     *
     * @param guild the Guild
     * @param level the Level of premium
     */
    @Override
    public void addPremiumForever(Guild guild, Level level) {
        log.debug("Request to add Premium Forever for Guild : {} with Level={}", guild.getName(), level.name);
        Optional<Premium> premiumOptional = findByDiscordId(guild.getIdLong());
        if ( premiumOptional.isPresent() ) {
            premiumOptional.get().setUntil(Instant.MAX);
            premiumOptional.get().setLevel(level.ordinal());
            save(premiumOptional.get());
        } else {
            Premium premium = new Premium();
            premium.setDiscordId(guild.getIdLong());
            premium.setLevel(level.ordinal());
            premium.setUntil(Instant.MAX);
            save(premium);
        }
        log.info("Premium Forever added for Guild: {}", guild.getName());
    }

    /**
     * Adds premium forever for a User
     *
     * @param user the User
     * @param level the Level of premium
     */
    @Override
    public void addPremiumForever(User user, Level level) {
        log.debug("Request to add Premium Forever for User : {} with Level={}", user.getName(), level.name);
        Optional<Premium> premiumOptional = findByDiscordId(user.getIdLong());
        if ( premiumOptional.isPresent() ) {
            premiumOptional.get().setUntil(Instant.MAX);
            premiumOptional.get().setLevel(level.ordinal());
            save(premiumOptional.get());
        } else {
            Premium premium = new Premium();
            premium.setDiscordId(user.getIdLong());
            premium.setLevel(level.ordinal());
            premium.setUntil(Instant.MAX);
            save(premium);
        }
        log.info("Premium Forever added for Guild: {}", user.getName());
    }

    /**
     * Add a premium level for a guild
     *
     * @param guild the Guild to add Premium to
     * @param level the Level of premium to add
     * @param time the time to add premium for
     * @param unit The unit of time
     */
    @Override
    public void addPremium(Guild guild, Level level, int time, TemporalUnit unit) {
        log.debug("Request to add Premium with Level={} for Guild={}", level.name, guild.getName());
        Optional<Premium> premiumOptional = findByDiscordId(guild.getIdLong());
        if ( premiumOptional.isPresent() ) {
            premiumOptional.get().setLevel(level.ordinal());
            Instant current = premiumOptional.get().getUntil();
            if(current.getEpochSecond() != Instant.MAX.getEpochSecond())
            {
                Instant now = Instant.now();
                premiumOptional.get().setUntil(now.isBefore(current) ? current.plus(time, unit) : now.plus(time, unit));
            }
            save(premiumOptional.get());
        } else {
            Premium premium = new Premium();
            premium.setDiscordId(guild.getIdLong());
            premium.setLevel(level.ordinal());
            premium.setUntil(Instant.now().plus(time, unit));
            save(premium);
        }
        log.info("Premium with Level={} added to Guild={}", level.name, guild.getName());
    }

    /**
     * Add a premium level for a User
     *
     * @param user the User to add Premium to
     * @param level the Level of premium to add
     * @param time the time to add premium for
     * @param unit The unit of time
     */
    @Override
    public void addPremium(User user, Level level, int time, TemporalUnit unit) {
        log.debug("Request to add Premium with Level={} for User={}", level.name, user.getName());
        Optional<Premium> premiumOptional = findByDiscordId(user.getIdLong());
        if ( premiumOptional.isPresent() ) {
            premiumOptional.get().setLevel(level.ordinal());
            Instant current = premiumOptional.get().getUntil();
            if(current.getEpochSecond() != Instant.MAX.getEpochSecond())
            {
                Instant now = Instant.now();
                premiumOptional.get().setUntil(now.isBefore(current) ? current.plus(time, unit) : now.plus(time, unit));
            }
            save(premiumOptional.get());
        } else {
            Premium premium = new Premium();
            premium.setDiscordId(user.getIdLong());
            premium.setLevel(level.ordinal());
            premium.setUntil(Instant.now().plus(time, unit));
            save(premium);
        }
        log.info("Premium with Level={} added to User={}", level.name, user.getName());
    }

    /**
     * Cancels the premium membership of a guild
     *
     * @param guild the guild to cancel
     */
    @Override
    public void cancelPremium(Guild guild) throws NoPremiumFoundException {
        log.debug("Request to cancel Premium for Guild={}", guild.getName());
        Optional<Premium> premiumOptional = findByDiscordId(guild.getIdLong());
        if ( premiumOptional.isPresent() ) {
            delete(premiumOptional.get().getId());
            log.info("Premium removed from Guild={}", guild.getName());
        } else {
            throw new NoPremiumFoundException(String.format("Cannot remove premium from Guild=%s:%d. No existing premium found"
                , guild.getName(), guild.getIdLong()));
        }
    }

    /**
     * Cancels the premium membership of a user
     *
     * @param user the User to cancel
     * @throws NoPremiumFoundException No premium membership was found for user
     */
    @Override
    public void cancelPremium(User user) throws NoPremiumFoundException {
        log.debug("Request to cancel Premium for User={}", user.getName());
        Optional<Premium> premiumOptional = findByDiscordId(user.getIdLong());
        if ( premiumOptional.isPresent() ) {
            delete(premiumOptional.get().getId());
            log.info("Premium removed from User={}", user.getName());
        } else {
            throw new NoPremiumFoundException(String.format("Cannot remove premium from User=%s:%d. No existing premium found"
                , user.getName(), user.getIdLong()));
        }
    }

    /**
     * Removes premium for all expired memberships
     *
     * @return list of removed guild ids
     */
    @Override
    public List<Long> cleanPremiumList()
    {
       log.info("Request to clean Premium expirations");
        List<Premium> premiumList = findAllByUntilIsLessThan(Instant.now());
        List<Long> list = new LinkedList<>();

        if (!premiumList.isEmpty()) {
            for ( Premium premium:  premiumList ) {
                list.add(premium.getDiscordId());
                delete(premium.getId());
            }
        }
        for ( Long guildId : list ){
            log.info("Removed premium for Guild={}", guildId);
        }
        return list;
    }


}
