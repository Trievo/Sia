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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.trievosoftware.application.domain.Punishment;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.commands.CommandExceptionListener;
import com.trievosoftware.discord.utils.OtherUtil;
import net.dv8tion.jda.core.Permission;

/**
 *
 * @author Mark Tripoli (mark.tripoli@trievosoftware.com)
 */
public class AutodehoistCmd extends Command
{
    private final Sia sia;
    
    public AutodehoistCmd(Sia sia)
    {
        this.sia = sia;
        this.name = "autodehoist";
        this.guildOnly = true;
        this.aliases = new String[]{"auto-dehoist"};
        this.category = new Category("AutoMod");
        this.arguments = "<character | OFF>";
        this.help = "prevents name-hoisting via usernames or nicknames";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    protected void execute(CommandEvent event)
    {
        if(event.getArgs().isEmpty())
            throw new CommandExceptionListener.CommandErrorException("Please provide a valid dehoist character, or OFF");
        else if(event.getArgs().equalsIgnoreCase("none") || event.getArgs().equalsIgnoreCase("off"))
        {
            sia.getServiceManagers().getAutoModService().setDehoistChar(event.getGuild(), (char)0);
            event.replySuccess("No action will be taken on name hoisting.");
            return;
        }
        char symbol;
        if(event.getArgs().length()==1)
            symbol = event.getArgs().charAt(0);
        else
            throw new CommandExceptionListener.CommandErrorException("Provided symbol must be one character of the following: "+OtherUtil.DEHOIST_JOINED);
        boolean allowed = false;
        for(char c: OtherUtil.DEHOIST_ORIGINAL)
            if(c==symbol)
                allowed = true;
        if(!allowed)
            throw new CommandExceptionListener.CommandErrorException("Provided symbol must be one character of the following: "+OtherUtil.DEHOIST_JOINED);
        
        sia.getServiceManagers().getAutoModService().setDehoistChar(event.getGuild(), symbol);
        boolean also = sia.getServiceManagers().getActionsService().useDefaultSettings(event.getGuild());
        event.replySuccess("Users will now be dehoisted if their effective name starts with `"+symbol+"` or higher."+
            (also ? Punishment.DEFAULT_SETUP_MESSAGE : ""));
    }
}
