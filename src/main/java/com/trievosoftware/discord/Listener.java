/*
 * Copyright 2018 John Grosh (john.a.grosh@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.trievosoftware.discord;

import com.trievosoftware.discord.logging.MessageCache.CachedMessage;
import net.dv8tion.jda.core.JDA.ShardInfo;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.core.events.guild.member.*;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Listener implements EventListener
{
    private final static Logger LOG = LoggerFactory.getLogger("Listener");
    private final Sia sia;
    
    public Listener(Sia sia)
    {
        this.sia = sia;
    }

    @Override
    public void onEvent(Event event)
    {
        if (event instanceof GuildMessageReceivedEvent)
        {
            Message m = ((GuildMessageReceivedEvent)event).getMessage();
            
            if(!m.getAuthor().isBot()) // ignore bot messages
            {
                // Store the message
                sia.getMessageCache().putMessage(m);
                
                // Run automod on the message
                sia.getAutoMod().performAutomod(m);
            }
        }
        else if (event instanceof GuildMessageUpdateEvent)
        {
            Message m = ((GuildMessageUpdateEvent)event).getMessage();
            
            if(!m.getAuthor().isBot()) // ignore bot edits
            {
                // Run automod on the message
                sia.getAutoMod().performAutomod(m);
                
                // Store and log the edit
                CachedMessage old = sia.getMessageCache().putMessage(m);
                sia.getBasicLogger().logMessageEdit(m, old);
            }
        }
        else if (event instanceof GuildMessageDeleteEvent)
        {
            GuildMessageDeleteEvent gevent = (GuildMessageDeleteEvent) event;
            
            // Log the deletion
            CachedMessage old = sia.getMessageCache().pullMessage(gevent.getGuild(), gevent.getMessageIdLong());
            sia.getBasicLogger().logMessageDelete(old);
        }
        else if (event instanceof MessageBulkDeleteEvent)
        {
            MessageBulkDeleteEvent gevent = (MessageBulkDeleteEvent) event;
            
            // Get the messages we had cached
            List<CachedMessage> logged = gevent.getMessageIds().stream()
                    .map(id -> sia.getMessageCache().pullMessage(gevent.getGuild(), Long.parseLong(id)))
                    .filter(m -> m!=null)
                    .collect(Collectors.toList());
            
            // Log the deletion
            sia.getBasicLogger().logMessageBulkDelete(logged, gevent.getMessageIds().size(), gevent.getChannel());
        }
        else if (event instanceof GuildMemberJoinEvent)
        {
            GuildMemberJoinEvent gevent = (GuildMemberJoinEvent) event;
            
            // Log the join
            sia.getBasicLogger().logGuildJoin(gevent);
            
            // Perform automod on the newly-joined member
            sia.getAutoMod().memberJoin(gevent);
        }
        else if (event instanceof GuildMemberLeaveEvent)
        {
            GuildMemberLeaveEvent gmle = (GuildMemberLeaveEvent)event;
            
            // Log the member leaving
            sia.getBasicLogger().logGuildLeave(gmle);
            
            // Signal the modlogger because someone might have been kicked
            sia.getModLogger().setNeedUpdate(gmle.getGuild());
        }
        else if (event instanceof GuildBanEvent)
        {
            // Signal the modlogger because someone was banned
            sia.getModLogger().setNeedUpdate(((GuildBanEvent) event).getGuild());
        }
        else if (event instanceof GuildUnbanEvent)
        {
            GuildUnbanEvent gue = (GuildUnbanEvent) event;
            // Signal the modlogger because someone was unbanned
            sia.getDatabase().tempbans.clearBan(gue.getGuild(), gue.getUser().getIdLong());
            sia.getModLogger().setNeedUpdate((gue).getGuild());
        }
        else if (event instanceof GuildMemberRoleAddEvent)
        {
            GuildMemberRoleAddEvent gmrae = (GuildMemberRoleAddEvent) event;
            
            // Signal the modlogger if someone was muted
            Role mRole = sia.getDatabase().settings.getSettings(gmrae.getGuild()).getMutedRole(gmrae.getGuild());
            if(gmrae.getRoles().contains(mRole))
                sia.getModLogger().setNeedUpdate(gmrae.getGuild());
        }
        else if (event instanceof GuildMemberRoleRemoveEvent)
        {
            GuildMemberRoleRemoveEvent gmrre = (GuildMemberRoleRemoveEvent) event;
            
            // Signal the modlogger if someone was unmuted
            Role mRole = sia.getDatabase().settings.getSettings(gmrre.getGuild()).getMutedRole(gmrre.getGuild());
            if(gmrre.getRoles().contains(mRole))
            {
                sia.getDatabase().tempmutes.removeMute(gmrre.getGuild(), gmrre.getUser().getIdLong());
                sia.getModLogger().setNeedUpdate(gmrre.getGuild());
            }
        }
        else if (event instanceof UserUpdateNameEvent)
        {
            UserUpdateNameEvent unue = (UserUpdateNameEvent)event;
            // Log the name change
            sia.getBasicLogger().logNameChange(unue);
            unue.getUser().getMutualGuilds().stream().map(g -> g.getMember(unue.getUser())).forEach(m -> sia.getAutoMod().dehoist(m));
        }
        else if (event instanceof UserUpdateDiscriminatorEvent)
        {
            sia.getBasicLogger().logNameChange((UserUpdateDiscriminatorEvent)event);
        }
        else if (event instanceof GuildMemberNickChangeEvent)
        {
            sia.getAutoMod().dehoist(((GuildMemberNickChangeEvent) event).getMember());
        }
        else if (event instanceof UserUpdateAvatarEvent)
        {
            UserUpdateAvatarEvent uaue = (UserUpdateAvatarEvent)event;
            
            // Log the avatar change
            if(!uaue.getUser().isBot())
                sia.getBasicLogger().logAvatarChange(uaue);
        }
        else if (event instanceof GuildVoiceJoinEvent)
        {
            GuildVoiceJoinEvent gevent = (GuildVoiceJoinEvent)event;
            
            // Log the voice join
            if(!gevent.getMember().getUser().isBot()) // ignore bots
                sia.getBasicLogger().logVoiceJoin(gevent);
        }
        else if (event instanceof GuildVoiceMoveEvent)
        {
            GuildVoiceMoveEvent gevent = (GuildVoiceMoveEvent)event;
            
            // Log the voice move
            if(!gevent.getMember().getUser().isBot()) // ignore bots
                sia.getBasicLogger().logVoiceMove(gevent);
        }
        else if (event instanceof GuildVoiceLeaveEvent)
        {
            GuildVoiceLeaveEvent gevent = (GuildVoiceLeaveEvent)event;
            
            // Log the voice leave
            if(!gevent.getMember().getUser().isBot()) // ignore bots
                sia.getBasicLogger().logVoiceLeave(gevent);
        }
        else if (event instanceof ReadyEvent)
        {
            // Log the shard that has finished loading
            ShardInfo si = event.getJDA().getShardInfo();
            String shardinfo = si==null ? "1/1" : (si.getShardId()+1)+"/"+si.getShardTotal();
            LOG.info("Shard "+shardinfo+" is ready.");
            sia.getLogWebhook().send("\uD83C\uDF00 Shard `"+shardinfo+"` has connected. Guilds: `" // 🌀
                    +event.getJDA().getGuildCache().size()+"` Users: `"+event.getJDA().getUserCache().size()+"`");
            sia.getThreadpool().scheduleWithFixedDelay(() -> sia.getDatabase().tempbans.checkUnbans(event.getJDA()), 0, 2, TimeUnit.MINUTES);
            sia.getThreadpool().scheduleWithFixedDelay(() -> sia.getDatabase().tempmutes.checkUnmutes(event.getJDA(), sia.getDatabase().settings), 0, 45, TimeUnit.SECONDS);
        }
    }
}
