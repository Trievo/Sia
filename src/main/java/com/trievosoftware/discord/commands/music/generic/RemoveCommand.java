/*
 *    Copyright 2019 Mark Tripoli
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.trievosoftware.discord.commands.music.generic;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.trievosoftware.application.domain.GuildMusicSettings;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.commands.meta.AbstractMusicCommand;
import com.trievosoftware.discord.music.audio.AudioHandler;
import com.trievosoftware.discord.music.audio.QueuedTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

public class RemoveCommand extends AbstractMusicCommand
{

    public RemoveCommand(Sia sia)
    {
        super(sia);
        this.name = "remove";
        this.help = "removes a song from the queue";
        this.arguments = "<position|ALL>";
        this.aliases = new String[]{"delete"};
        this.beListening = true;
        this.bePlaying = true;
}

    @Override
    public void doCommand(CommandEvent event)
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if(handler.getQueue().isEmpty())
        {
            event.replyError("There is nothing in the queue!");
            return;
        }
        if(event.getArgs().equalsIgnoreCase("all"))
        {
            int count = handler.getQueue().removeAll(event.getAuthor().getIdLong());
            if(count==0)
                event.replyWarning("You don't have any songs in the queue!");
            else
                event.replySuccess("Successfully removed your "+count+" entries.");
            return;
        }
        int pos;
        try {
            pos = Integer.parseInt(event.getArgs());
        } catch(NumberFormatException e) {
            pos = 0;
        }
        if(pos<1 || pos>handler.getQueue().size())
        {
            event.replyError("Position must be a valid integer between 1 and "+handler.getQueue().size()+"!");
            return;
        }
        //Settings settings = event.getClient().getSettingsFor(event.getGuild());
        GuildMusicSettings settings = sia.getServiceManagers().getGuildMusicSettingsService().getSettings(event.getGuild());
        boolean isDJ = event.getMember().hasPermission(Permission.MANAGE_SERVER);
        if(!isDJ)
            isDJ = event.getMember().getRoles().contains(settings.getDjRole(event.getGuild()));
        QueuedTrack qt = handler.getQueue().get(pos-1);
        if(qt.getIdentifier()==event.getAuthor().getIdLong())
        {
            handler.getQueue().remove(pos-1);
            event.replySuccess("Removed **"+qt.getTrack().getInfo().title+"** from the queue");
        }
        else if(isDJ)
        {
            handler.getQueue().remove(pos-1);
            User u;
            try {
                u = event.getJDA().getUserById(qt.getIdentifier());
            } catch(Exception e) {
                u = null;
            }
            event.replySuccess("Removed **"+qt.getTrack().getInfo().title
                +"** from the queue (requested by "+(u==null ? "someone" : "**"+u.getName()+"**")+")");
        }
        else
        {
            event.replyError("You cannot remove **"+qt.getTrack().getInfo().title+"** because you didn't add it!");
        }
    }
}
