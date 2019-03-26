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

package com.trievosoftware.discord.commands.music.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.trievosoftware.application.exceptions.NoMusicSettingsException;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.commands.meta.AbstractDjCommand;
import com.trievosoftware.discord.music.audio.AudioHandler;
import com.trievosoftware.discord.utils.FormatUtil;

public class VolumeCommand extends AbstractDjCommand {

    public VolumeCommand(Sia sia) {
        super(sia);
        this.name = "volume";
        this.aliases = new String[]{"vol"};
        this.help = "sets or shows volume";
        this.arguments = "[0-150]";
    }

    @Override
    public void doCommand(CommandEvent event) throws NoMusicSettingsException {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        int volume = handler.getPlayer().getVolume();
        if(event.getArgs().isEmpty())
        {
            event.reply(FormatUtil.volumeIcon(volume)+" Current volume is `"+volume+"`");
        }
        else
        {
            int nvolume;
            try{
                nvolume = Integer.parseInt(event.getArgs());
            }catch(NumberFormatException e){
                nvolume = -1;
            }
            if(nvolume<0 || nvolume>150)
                event.reply(event.getClient().getError()+" Volume must be a valid integer between 0 and 150!");
            else
            {
                handler.getPlayer().setVolume(nvolume);
                sia.getServiceManagers().getGuildMusicSettingsService().setVolume(event.getGuild(), nvolume);
                event.reply(FormatUtil.volumeIcon(nvolume)+" Volume changed from `"+volume+"` to `"+nvolume+"`");
            }
        }
    }
}
