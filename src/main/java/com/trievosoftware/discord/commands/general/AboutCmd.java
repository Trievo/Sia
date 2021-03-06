/*
 * Copyright 2018 Mark Tripoli (mark.tripoli@trievosoftware.com).
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
package com.trievosoftware.discord.commands.general;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.trievosoftware.discord.Constants;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.commands.meta.AbstractGenericCommand;
import com.trievosoftware.discord.utils.FormatUtil;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;

import java.awt.*;

/**
 *
 * @author Mark Tripoli (mark.tripoli@trievosoftware.com)
 */
public class AboutCmd extends AbstractGenericCommand
{
    public AboutCmd(Sia sia)
    {
        super(sia);
        this.name = "about";
        this.help = "shows info about the bot";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        ShardManager sm = event.getJDA().asBot().getShardManager();
        event.reply(new MessageBuilder()
                .setContent(Constants.SIA_EMOJII + " **All about Sia** " + Constants.SIA_EMOJII)
                .setEmbed(new EmbedBuilder()
                        .setColor(event.getGuild()==null ? Color.GRAY : event.getSelfMember().getColor())
                        .setDescription("Hello, I am **Sia**#8540, a bot designed to keep your server safe and make moderating fast and easy!\n"
                                + "I was written in Java by **triippz**#0689 using [JDA](" + JDAInfo.GITHUB + ") and [JDA-Utilities](" + JDAUtilitiesInfo.GITHUB + ")\n"
                                + "Type `" + event.getClient().getPrefix() + event.getClient().getHelpWord() + "` for help and information.\n\n"
                                + FormatUtil.helpLinks(event))
                        .addField("Stats", sm.getShardsTotal()+ " Shards\n" + sm.getGuildCache().size() + " Servers", true)
                        .addField("", sm.getUserCache().size() + " Users\n" + Math.round(sm.getAveragePing()) + "ms Avg Ping", true)
                        .addField("", sm.getTextChannelCache().size() + " Text Channels\n" + sm.getVoiceChannelCache().size() + " Voice Channels", true)
                        .setFooter("Last restart", null)
                        .setTimestamp(event.getClient().getStartTime())
                        .build())
                .build());
    }
}
