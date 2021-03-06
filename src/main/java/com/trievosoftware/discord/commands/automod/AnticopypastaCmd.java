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
package com.trievosoftware.discord.commands.automod;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.trievosoftware.application.domain.AutoMod;
import com.trievosoftware.application.domain.Punishment;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.commands.meta.AbstractModeratorCommand;
import net.dv8tion.jda.core.Permission;

/**
 *
 * @author Mark Tripoli (mark.tripoli@trievosoftware.com)
 */
public class AnticopypastaCmd extends AbstractModeratorCommand
{
    public AnticopypastaCmd(Sia sia)
    {
        super(sia);
        this.name = "anticopypasta";
        this.guildOnly = true;
        this.aliases = new String[]{"antipasta","anti-copypasta"};
        this.category = new Category("AutoMod");
        this.arguments = "<strikes>";
        this.help = "sets strikes for posting copypastas";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void doCommand(CommandEvent event)
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("Please provide a number of strikes!");
            return;
        }
        int numstrikes;
        try
        {
            numstrikes = Integer.parseInt(event.getArgs());
        }
        catch(NumberFormatException ex)
        {
            if(event.getArgs().equalsIgnoreCase("none") || event.getArgs().equalsIgnoreCase("off"))
                numstrikes = 0;
            else
            {
                event.replyError("`"+event.getArgs()+"` is not a valid integer!");
                return;
            }
        }
        if(numstrikes<0 || numstrikes> AutoMod.MAX_STRIKES)
        {
            event.replyError("The number of strikes must be between 0 and "+ AutoMod.MAX_STRIKES);
            return;
        }
        sia.getServiceManagers().getAutoModService().setCopypastaStrikes(event.getGuild(), numstrikes);
        boolean also = sia.getServiceManagers().getActionsService().useDefaultSettings(event.getGuild());
        event.replySuccess("Users will now receive `"+numstrikes+"` strikes for posting copypastas."+(also ? Punishment.DEFAULT_SETUP_MESSAGE : ""));
    }
}
