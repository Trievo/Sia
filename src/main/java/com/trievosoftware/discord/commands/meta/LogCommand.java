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
package com.trievosoftware.discord.commands.meta;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.trievosoftware.discord.Constants;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.utils.FormatUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

/**
 *
 * @author Mark Tripoli (mark.tripoli@trievosoftware.com)
 */
public abstract class LogCommand extends Command
{
    protected static Permission[] REQUIRED_PERMS = {Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_HISTORY};
    protected static String REQUIRED_ERROR = "I am missing the necessary permissions (Read Messages, Send Messages, Read Message History, and Embed Links) in %s!";
    protected final Sia sia;
    
    public LogCommand(Sia sia)
    {
        this.sia = sia;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.guildOnly = true;
        this.arguments = "<#channel or OFF>";
        this.category = new Category("Settings");
    }

    @Override
    protected void execute(CommandEvent event)
    {
        if ( sia.getServiceManagers().getDiscordUserService().isUserBlacklisted(event.getAuthor().getIdLong()))
        {
            event.replyError("You are not authorized to use this bot. If you feel like this is a mistake please contact" +
                "the creators via discord: " + Constants.SERVER_INVITE);
            return;
        }
        if(event.getArgs().isEmpty())
        {
            showCurrentChannel(event);
            return;
        }
        if(event.getArgs().equalsIgnoreCase("off") || event.getArgs().equalsIgnoreCase("none"))
        {
            setLogChannel(event, null);
            return;
        }
        List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
        if(list.isEmpty())
        {
            event.replyError("I couldn't find any text channel called `"+event.getArgs()+"`.");
            return;
        }
        if(list.size()>1)
        {
            event.replyWarning(FormatUtil.listOfText(list, event.getArgs()));
            return;
        }
        
        TextChannel tc = list.get(0);
        
        if(!event.getSelfMember().hasPermission(tc, REQUIRED_PERMS))
        {
            event.replyError(String.format(REQUIRED_ERROR, tc.getAsMention()));
            return;
        }
        
        setLogChannel(event, tc);
    }
    
    protected abstract void showCurrentChannel(CommandEvent event);
    
    protected abstract void setLogChannel(CommandEvent event, TextChannel tc);
}
