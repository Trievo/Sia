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
package com.trievosoftware.discord.commands.tools;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.trievosoftware.discord.Constants;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.commands.CommandExceptionListener.CommandErrorException;
import com.trievosoftware.discord.commands.CommandExceptionListener.CommandWarningException;
import com.trievosoftware.discord.commands.meta.AbstractGenericCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Invite;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @author Mark Tripoli (mark.tripoli@trievosoftware.com)
 */
public class InvitepruneCmd extends AbstractGenericCommand
{
    private final static String CANCEL = "\u274C"; // ❌
    private final static String CONFIRM = "\u2611"; // ☑

    public InvitepruneCmd(Sia sia)
    {
        super(sia);
        this.name = "inviteprune";
        this.arguments = "[max uses]";
        this.help = "deletes invites with up to a certain number of uses";
        this.category = new Category("Tools");
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.botPermissions = new Permission[]{Permission.MANAGE_SERVER};
        this.guildOnly = true;
        this.cooldown = 10;
    }
    
    @Override
    public void doCommand(CommandEvent event)
    {
        int uses;
        if(event.getArgs().isEmpty())
            uses = 1;
        else try
        {
            uses = Integer.parseInt(event.getArgs());
        }
        catch(NumberFormatException ex)
        {
            throw new CommandErrorException("`"+event.getArgs()+"` is not a valid integer!");
        }
        if(uses<0 || uses>50)
            throw new CommandWarningException("Maximum uses must be at least 0 and no larger than 50");
        if(uses>10)
            waitForConfirmation(event, "This will delete all invites with "+uses+" or fewer uses.", () -> pruneInvites(uses, event));
        else
            pruneInvites(uses, event);
    }
    
    private void pruneInvites(int uses, CommandEvent event)
    {
        event.getChannel().sendTyping().queue();
        event.getGuild().getInvites().queue(list -> 
        {
            List<Invite> toPrune = list.stream().filter(i -> i.getInviter()!=null && !i.getInviter().isBot() && i.getUses()<=uses).collect(Collectors.toList());
            toPrune.forEach(i -> i.delete().queue());
            event.replySuccess("Deleting `"+toPrune.size()+"` invites with `"+uses+"` or fewer uses.");
        });
    }

    @SuppressWarnings("Duplicates")
    private void waitForConfirmation(CommandEvent event, String message, Runnable confirm)
    {
        new ButtonMenu.Builder()
                .setChoices(CONFIRM, CANCEL)
                .setEventWaiter(sia.getEventWaiter())
                .setTimeout(1, TimeUnit.MINUTES)
                .setText(Constants.WARNING+" "+message+"\n\n"+CONFIRM+" Continue\n"+CANCEL+" Cancel")
                .setFinalAction(m -> m.delete().queue(s->{}, f->{}))
                .setUsers(event.getAuthor())
                .setAction(re ->
                {
                    if(re.getName().equals(CONFIRM))
                        confirm.run();
                }).build().display(event.getChannel());
    }
}
