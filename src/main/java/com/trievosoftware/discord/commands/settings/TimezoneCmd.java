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
package com.trievosoftware.discord.commands.settings;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.trievosoftware.discord.Constants;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.commands.meta.AbstractModeratorCommand;
import net.dv8tion.jda.core.Permission;

import java.time.ZoneId;

/**
 *
 * @author Mark Tripoli (mark.tripoli@trievosoftware.com)
 */
public class TimezoneCmd extends AbstractModeratorCommand
{
    public TimezoneCmd(Sia sia)
    {
        super(sia);
        this.name = "timezone";
        this.help = "sets the log timezone";
        this.arguments = "<zone>";
        this.category = new Category("Settings");
        this.guildOnly = true;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("Please include a time zone. A full list of timezones can be found here: <"+ Constants.Wiki.LOG_TIMEZONE+">");
            return;
        }
        
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            sia.getServiceManagers().getGuildSettingsService().setTimezone(event.getGuild(), null);
            event.replySuccess("The log timezone has been reset.");
            return;
        }
        
        try
        {
            ZoneId newzone = ZoneId.of(event.getArgs());
            sia.getServiceManagers().getGuildSettingsService().setTimezone(event.getGuild(), newzone);
            event.replySuccess("The log timezone has been set to `"+newzone.getId()+"`");
        }
        catch(Exception ex)
        {
            event.replyError("`"+event.getArgs()+"` is not a valid timezone! See <"+ Constants.Wiki.LOG_TIMEZONE+"> for a full list.");
        }
    }
}
