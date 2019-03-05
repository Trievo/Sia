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
package com.trievosoftware.discord.commands.settings;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.trievosoftware.discord.Constants;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.commands.LogCommand;
import com.trievosoftware.discord.database.managers.PremiumManager;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class AvatarlogCmd extends LogCommand
{
    public AvatarlogCmd(Sia sia)
    {
        super(sia);
        this.name = "avatarlog";
        this.help = "sets channel to log avatar changes";
    }

    @Override
    protected void showCurrentChannel(CommandEvent event)
    {
        TextChannel tc = sia.getDatabase().settings.getSettings(event.getGuild()).getAvatarLogChannel(event.getGuild());
        if(tc==null)
            event.replyWarning("Avatar Logs are not currently enabled on the server. Please include a channel name.");
        else
            event.replySuccess("Avatar Logs are currently being sent in "+tc.getAsMention()
                    +(event.getSelfMember().hasPermission(tc, REQUIRED_PERMS) ? "" : "\n"+event.getClient().getWarning()+String.format(REQUIRED_ERROR, tc.getAsMention())));
    }

    @Override
    protected void setLogChannel(CommandEvent event, TextChannel tc)
    {
        if(sia.getDatabase().premium.getPremiumInfo(event.getGuild()).level.isAtLeast(PremiumManager.Level.PRO))
        {
            sia.getDatabase().settings.setAvatarLogChannel(event.getGuild(), tc);
            if(tc==null)
                event.replySuccess("Avatar Logs will not be sent");
            else
                event.replySuccess("Avatar Logs will now be sent in "+tc.getAsMention());
        }
        else
            event.reply(Constants.NEED_PRO);
    }
}
