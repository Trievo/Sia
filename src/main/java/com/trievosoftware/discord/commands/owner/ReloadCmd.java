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
package com.trievosoftware.discord.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.commands.CommandExceptionListener.CommandErrorException;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class ReloadCmd extends Command
{
    private final Sia sia;
    
    public ReloadCmd(Sia sia)
    {
        this.sia = sia;
        this.name = "reload";
        this.arguments = "<ref|safe|copy>";
        this.help = "reloads a file";
        this.ownerCommand = true;
        this.guildOnly = false;
        this.hidden = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        switch(event.getArgs().toLowerCase())
        {
            case "ref":
                sia.getAutoMod().loadReferralDomains();
                event.replySuccess("Reloaded ref domains");
                break;
            case "safe":
                sia.getAutoMod().loadSafeDomains();
                event.replySuccess("Reloaded safe domains");
                break;
            case "copy":
                sia.getAutoMod().loadCopypastas();
                event.replySuccess("Reloaded copypastas");
                break;
            default:
                throw new CommandErrorException("Invalid reload selection: `ref` `safe` `copy`");
        }
    }
}