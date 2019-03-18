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
import com.jagrosh.jlyrics.LyricsClient;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.commands.meta.AbstractMusicCommand;
import com.trievosoftware.discord.music.audio.AudioHandler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;

public class LyricsCommand extends AbstractMusicCommand {

    private final LyricsClient client = new LyricsClient();

    public LyricsCommand(Sia sia) {
        super(sia);
        this.name = "lyrics";
        this.arguments = "[song name]";
        this.help = "shows the lyrics to the currently-playing song";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        event.getChannel().sendTyping().queue();
        String title;
        if(event.getArgs().isEmpty())
            title = ((AudioHandler)event.getGuild().getAudioManager().getSendingHandler()).getPlayer().getPlayingTrack().getInfo().title;
        else
            title = event.getArgs();
        client.getLyrics(title).thenAccept(lyrics ->
        {
            if(lyrics == null)
            {
                event.replyError("Lyrics for `" + title + "` could not be found!");
                return;
            }

            EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(lyrics.getAuthor())
                .setColor(event.getSelfMember().getColor())
                .setTitle(lyrics.getTitle(), lyrics.getURL());
            if(lyrics.getContent().length()>15000)
            {
                event.replyWarning("Lyrics for `" + title + "` found but likely not correct: " + lyrics.getURL());
            }
            else if(lyrics.getContent().length()>2000)
            {
                String content = lyrics.getContent().trim();
                while(content.length() > 2000)
                {
                    int index = content.lastIndexOf("\n\n", 2000);
                    if(index == -1)
                        index = content.lastIndexOf("\n", 2000);
                    if(index == -1)
                        index = content.lastIndexOf(" ", 2000);
                    if(index == -1)
                        index = 2000;
                    event.reply(eb.setDescription(content.substring(0, index).trim()).build());
                    content = content.substring(index).trim();
                    eb.setAuthor(null).setTitle(null, null);
                }
                event.reply(eb.setDescription(content).build());
            }
            else
                event.reply(eb.setDescription(lyrics.getContent()).build());
        });
    }
}
