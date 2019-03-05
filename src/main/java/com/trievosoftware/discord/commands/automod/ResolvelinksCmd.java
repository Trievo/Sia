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
package com.trievosoftware.discord.commands.automod;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.trievosoftware.discord.Constants;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.database.managers.PremiumManager;
import net.dv8tion.jda.core.Permission;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class ResolvelinksCmd extends Command
{
    private final Sia sia;
    private final static String DESCRIPTION = "When link resolving is enabled, all links will be checked for redirects when performing automod functions.";
    
    public ResolvelinksCmd(Sia sia)
    {
        this.sia = sia;
        this.name = "resolvelinks";
        this.guildOnly = true;
        this.category = new Category("AutoMod");
        this.arguments = "<ON|OFF>";
        this.help = "resolve redirect urls";
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    protected void execute(CommandEvent event)
    {
        if(event.getArgs().equalsIgnoreCase("on") || event.getArgs().equalsIgnoreCase("off"))
        {
            if(sia.getDatabase().premium.getPremiumInfo(event.getGuild()).level.isAtLeast(PremiumManager.Level.PRO))
            {
                sia.getDatabase().automod.setResolveUrls(event.getGuild(), event.getArgs().equalsIgnoreCase("on"));
                event.replySuccess("Link Resolving has been turned `"+event.getArgs().toUpperCase()+"`");
            }
            else
                event.reply(Constants.NEED_PRO);
        }
        else
        {
            event.replyWarning(DESCRIPTION+"\nValid options are `ON` and `OFF`");
        }
        
    }
}
