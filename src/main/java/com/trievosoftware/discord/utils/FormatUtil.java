/*
 * Copyright 2016 John Grosh (jagrosh).
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
package com.trievosoftware.discord.utils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.trievosoftware.application.domain.*;
import com.trievosoftware.application.exceptions.StringNotIntegerException;
import com.trievosoftware.discord.Constants;
import com.trievosoftware.discord.Sia;
import com.trievosoftware.discord.logging.MessageCache.CachedMessage;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Mark Tripoli (triippz)
 */
@SuppressWarnings("Duplicates")
public class FormatUtil {
    
    private final static String MULTIPLE_FOUND = "**Multiple %s found matching \"%s\":**";
    private final static String CMD_EMOJI = "\uD83D\uDCDC"; // 📜
    private final static String POLL_EMOJI = "\uD83D\uDCCA"; //📊
    private final static String PARTY_EMOJI = "\uD83C\uDF89"; //🎉
    
    public static String filterEveryone(String input)
    {
        return input.replace("@everyone","@\u0435veryone") // cyrillic e
                .replace("@here","@h\u0435re") // cyrillic e
                .replace("discord.gg/", "dis\u0441ord.gg/"); // cyrillic c
    }
    
    public static String formatMessage(Message m)
    {
        StringBuilder sb = new StringBuilder(m.getContentRaw());
        m.getAttachments().forEach(att -> sb.append("\n").append(att.getUrl()));
        return sb.length()>2048 ? sb.toString().substring(0, 2040) : sb.toString();
    }
    
    public static String formatMessage(CachedMessage m)
    {
        StringBuilder sb = new StringBuilder(m.getContentRaw());
        m.getAttachments().forEach(att -> sb.append("\n").append(att.getUrl()));
        return sb.length()>2048 ? sb.toString().substring(0, 2040) : sb.toString();
    }
    
    public static String formatFullUserId(long userId)
    {
        return "<@"+userId+"> (ID:"+userId+")";
    }
    
    public static String formatCachedMessageFullUser(CachedMessage msg)
    {
        return filterEveryone("**"+msg.getUsername()+"**#"+msg.getDiscriminator()+" (ID:"+msg.getAuthorId()+")");
    }
    
    public static String formatUser(User user)
    {
        return filterEveryone("**"+user.getName()+"**#"+user.getDiscriminator());
    }
    
    public static String formatFullUser(User user)
    {
        return filterEveryone("**"+user.getName()+"**#"+user.getDiscriminator()+" (ID:"+user.getId()+")");
    }
    
    public static String capitalize(String input)
    {
        if(input==null || input.isEmpty())
            return "";
        if(input.length()==1)
            return input.toUpperCase();
        return Character.toUpperCase(input.charAt(0))+input.substring(1).toLowerCase();
    }
    
    public static String join(String delimiter, char... items)
    {
        if(items==null || items.length==0)
            return "";
        StringBuilder sb = new StringBuilder().append(items[0]);
        for(int i=1; i<items.length; i++)
            sb.append(delimiter).append(items[i]);
        return sb.toString();
    }
    
    public static <T> String join(String delimiter, Function<T,String> function, T... items)
    {
        if(items==null || items.length==0)
            return "";
        StringBuilder sb = new StringBuilder(function.apply(items[0]));
        for(int i=1; i<items.length; i++)
            sb.append(delimiter).append(function.apply(items[i]));
        return sb.toString();
    }
    
    public static String listOfVoice(List<VoiceChannel> list, String query)
    {
        String out = String.format(MULTIPLE_FOUND, "voice channels", query);
        for(int i=0; i<6 && i<list.size(); i++)
            out+="\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6)
            out+="\n**And "+(list.size()-6)+" more...**";
        return out;
    }
    
    public static String listOfRoles(List<Role> list, String query)
    {
        String out = String.format(MULTIPLE_FOUND, "roles", query);
        for(int i=0; i<6 && i<list.size(); i++)
            out+="\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6)
            out+="\n**And "+(list.size()-6)+" more...**";
        return out;
    }
    
    public static String listOfText(List<TextChannel> list, String query)
    {
        String out = String.format(MULTIPLE_FOUND, "text channels", query);
        for(int i=0; i<6 && i<list.size(); i++)
            out+="\n - "+list.get(i).getName()+" ("+list.get(i).getAsMention()+")";
        if(list.size()>6)
            out+="\n**And "+(list.size()-6)+" more...**";
        return out;
    }
    
    public static String listOfUser(List<User> list, String query)
    {
        String out = String.format(MULTIPLE_FOUND, "users", query);
        for(int i=0; i<6 && i<list.size(); i++)
            out+="\n - **"+list.get(i).getName()+"**#"+list.get(i).getDiscriminator()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6)
            out+="\n**And "+(list.size()-6)+" more...**";
        return out;
    }
    
    public static String listOfMember(List<Member> list, String query)
    {
        String out = String.format(MULTIPLE_FOUND, "members", query);
        for(int i=0; i<6 && i<list.size(); i++)
            out+="\n - **"+list.get(i).getUser().getName()+"**#"+list.get(i).getUser().getDiscriminator()+" (ID:"+list.get(i).getUser().getId()+")";
        if(list.size()>6)
            out+="\n**And "+(list.size()-6)+" more...**";
        return out;
    }
    
    public static String secondsToTime(long timeseconds)
    {
        StringBuilder builder = new StringBuilder();
        int years = (int)(timeseconds / (60*60*24*365));
        if(years>0)
        {
            builder.append("**").append(years).append("** years, ");
            timeseconds = timeseconds % (60*60*24*365);
        }
        int weeks = (int)(timeseconds / (60*60*24*365));
        if(weeks>0)
        {
            builder.append("**").append(weeks).append("** weeks, ");
            timeseconds = timeseconds % (60*60*24*7);
        }
        int days = (int)(timeseconds / (60*60*24));
        if(days>0)
        {
            builder.append("**").append(days).append("** days, ");
            timeseconds = timeseconds % (60*60*24);
        }
        int hours = (int)(timeseconds / (60*60));
        if(hours>0)
        {
            builder.append("**").append(hours).append("** hours, ");
            timeseconds = timeseconds % (60*60);
        }
        int minutes = (int)(timeseconds / (60));
        if(minutes>0)
        {
            builder.append("**").append(minutes).append("** minutes, ");
            timeseconds = timeseconds % (60);
        }
        if(timeseconds>0)
            builder.append("**").append(timeseconds).append("** seconds");
        String str = builder.toString();
        if(str.endsWith(", "))
            str = str.substring(0,str.length()-2);
        if(str.isEmpty())
            str="**No time**";
        return str;
    }
    
    public static String secondsToTimeCompact(long timeseconds)
    {
        StringBuilder builder = new StringBuilder();
        int years = (int)(timeseconds / (60*60*24*365));
        if(years>0)
        {
            builder.append("**").append(years).append("**y ");
            timeseconds = timeseconds % (60*60*24*365);
        }
        int weeks = (int)(timeseconds / (60*60*24*365));
        if(weeks>0)
        {
            builder.append("**").append(weeks).append("**w ");
            timeseconds = timeseconds % (60*60*24*7);
        }
        int days = (int)(timeseconds / (60*60*24));
        if(days>0)
        {
            builder.append("**").append(days).append("**d ");
            timeseconds = timeseconds % (60*60*24);
        }
        int hours = (int)(timeseconds / (60*60));
        if(hours>0)
        {
            builder.append("**").append(hours).append("**h ");
            timeseconds = timeseconds % (60*60);
        }
        int minutes = (int)(timeseconds / (60));
        if(minutes>0)
        {
            builder.append("**").append(minutes).append("**m ");
            timeseconds = timeseconds % (60);
        }
        if(timeseconds>0)
            builder.append("**").append(timeseconds).append("**s");
        String str = builder.toString();
        if(str.endsWith(", "))
            str = str.substring(0,str.length()-2);
        if(str.isEmpty())
            str="**No time**";
        return str;
    }
    
    public static Message formatHelp(CommandEvent event, Sia sia)
    {
        EmbedBuilder builder = new EmbedBuilder()
            .setColor(event.getGuild()==null ? Color.LIGHT_GRAY : event.getSelfMember().getColor());
        
        List<Command> commandsInCategory;
        String content;
        if(event.getArgs().isEmpty())
        {
            commandsInCategory = Collections.EMPTY_LIST;
            content = Constants.SUCCESS+" **"+event.getSelfUser().getName()+"** Commands Categories:";
        }
        else
        {
            commandsInCategory = event.getClient().getCommands().stream().filter(cmd -> 
                    {
                        if(cmd.isHidden() || cmd.isOwnerCommand())
                            return false;
                        if(cmd.getCategory()==null)
                            return "general".startsWith(event.getArgs().toLowerCase());
                        return cmd.getCategory().getName().toLowerCase().startsWith(event.getArgs().toLowerCase());
                    }).collect(Collectors.toList());
            if(commandsInCategory.isEmpty())
                content = Constants.WARNING+" No Category `"+event.getArgs()+"` found.";
            else
                content = Constants.SUCCESS+" **"+event.getSelfUser().getName()+"** "
                        +(commandsInCategory.get(0).getCategory()==null ? "General" : commandsInCategory.get(0).getCategory().getName())
                        +" Commands:";
        }
        
        if(commandsInCategory.isEmpty())
        {
            builder.addField(CMD_EMOJI+" General Commands", "[**"+ Constants.PREFIX+"help general**]("+ Constants.Wiki.COMMANDS+"#-general-commands)\n\u200B", false);
            event.getClient().getCommands().stream().filter(cmd -> cmd.getCategory()!=null).map(cmd -> cmd.getCategory().getName()).distinct()
                    .forEach(cat -> builder.addField(CMD_EMOJI+" "+cat+" Commands", "[**"+ Constants.PREFIX+"help "+cat.toLowerCase()+"**]("
                            + Constants.Wiki.COMMANDS+"#-"+cat.toLowerCase()+"-commands)\n\u200B", false));
        }
        else
        {
            commandsInCategory.forEach(cmd -> builder.addField(Constants.PREFIX+cmd.getName()+(cmd.getArguments()==null ? "" : " "+cmd.getArguments()),
                    "[**"+cmd.getHelp()+"**]("+ Constants.Wiki.COMMANDS+"#-"+(cmd.getCategory()==null?"general":cmd.getCategory().getName().toLowerCase())+"-commands)\n\u200B", false));
        }
        
        builder.addField("Additional Help", helpLinks(event), false);
        
        return new MessageBuilder().append(content).setEmbed(builder.build()).build();
    }
    
    public static String helpLinks(CommandEvent event)
    {
        return "\uD83D\uDD17 ["+event.getSelfUser().getName()+" Wiki]("+ Constants.Wiki.WIKI_BASE+")\n" // 🔗
                + "<:discord:553202383774285825> [Support Server]("+event.getClient().getServerInvite()+")\n"
                +  CMD_EMOJI + " [Full Command Reference]("+ Constants.Wiki.COMMANDS+")\n"
                + "<:PayPal:553201177467289600> [Donations]("+ Constants.DONATION_LINK+")\n"
                + "<:xlm:555192428815056896> Donate XLM: " + Constants.XLM_DONATION_ADDR;
    }

    public static String helpLinksJoin(GuildJoinEvent event)
    {
        return "\uD83D\uDD17 ["+event.getJDA().getSelfUser().getName()+" Wiki]("+ Constants.Wiki.WIKI_BASE+")\n" // 🔗
            + "<:discord:553202383774285825> [Support Server]("+ Constants.SERVER_INVITE+")\n"
            +  CMD_EMOJI + " [Full Command Reference]("+ Constants.Wiki.COMMANDS+")\n"
            + "<:PayPal:553201177467289600> [Donations]("+ Constants.DONATION_LINK+")\n"
            + "<:xlm:555192428815056896> Donate XLM: " + Constants.XLM_DONATION_ADDR;
    }

    public static String formatTime(long duration)
    {
        if(duration == Long.MAX_VALUE)
            return "LIVE";
        long seconds = Math.round(duration/1000.0);
        long hours = seconds/(60*60);
        seconds %= 60*60;
        long minutes = seconds/60;
        seconds %= 60;
        return (hours>0 ? hours+":" : "") + (minutes<10 ? "0"+minutes : minutes) + ":" + (seconds<10 ? "0"+seconds : seconds);
    }

    public static String progressBar(double percent)
    {
        String str = "";
        for(int i=0; i<12; i++)
            if(i == (int)(percent*12))
                str+="\uD83D\uDD18"; // 🔘
            else
                str+="▬";
        return str;
    }

    public static String volumeIcon(int volume)
    {
        if(volume == 0)
            return "\uD83D\uDD07"; // 🔇
        if(volume < 30)
            return "\uD83D\uDD08"; // 🔈
        if(volume < 70)
            return "\uD83D\uDD09"; // 🔉
        return "\uD83D\uDD0A";     // 🔊
    }

    public static String filter(String input)
    {
        return input.replace("@everyone", "@\u0435veryone").replace("@here", "@h\u0435re").trim(); // cyrillic letter e
    }

    public static String listOfTChannels(List<TextChannel> list, String query)
    {
        String out = " Multiple text channels found matching \""+query+"\":";
        for(int i=0; i<6 && i<list.size(); i++)
            out+="\n - "+list.get(i).getName()+" (<#"+list.get(i).getId()+">)";
        if(list.size()>6)
            out+="\n**And "+(list.size()-6)+" more...**";
        return out;
    }

    public static String listOfVChannels(List<VoiceChannel> list, String query)
    {
        String out = " Multiple voice channels found matching \""+query+"\":";
        for(int i=0; i<6 && i<list.size(); i++)
            out+="\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6)
            out+="\n**And "+(list.size()-6)+" more...**";
        return out;
    }

    public static Integer getMinutes(String timeStr) throws StringNotIntegerException {
        String tempTime = timeStr;
        timeStr = timeStr.substring(0, timeStr.length() - 1);
        if ( timeStr.matches("\\d+"))
            return Integer.parseInt(timeStr);
        else
            throw new StringNotIntegerException("`" + tempTime + "`: is not a valid Time. Correct format is: `time:time_unit (1M)`");
    }

    public static MessageEmbed.Field getPollWinner(Set<PollItems> pollItemsSet)
    {
        Integer max = 0;
        String itemName = "";

        for ( PollItems pollItem : pollItemsSet )
        {
            if ( pollItem.getVotes() > max ) {
                max = pollItem.getVotes();
                itemName = pollItem.getItemName();
            }
        }

        return new MessageEmbed.Field(
            PARTY_EMOJI + "  **The Winner of the Poll!**  " + PARTY_EMOJI,
            "**`" + itemName.replaceAll("_", " ") + "` with `" + max + "` votes!**",
            false
        );
    }

    public static Message formatPollComplete(JDA jda, Poll poll)
    {
        MessageBuilder builder = new MessageBuilder();

        builder.setContent(POLL_EMOJI+ "  **Poll '" + poll.getTitle() + "` Results!**  " + POLL_EMOJI);

        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for ( PollItems pollItem : poll.getPollitems() )
        {
            stringBuilder.append(pollItem.getReaction()).append(". `").append(pollItem.getItemName().replaceAll("_", " ")).append("` : [")
                .append(pollItem.getVotes()).append(" votes]\n");
            i++;
        }

        MessageEmbed.Field field = getPollWinner(poll.getPollitems());

        builder.setEmbed(new EmbedBuilder()
            .setColor(jda.getGuildById(poll.getGuildId()).getSelfMember().getColor())
            .setDescription(stringBuilder.toString())
            .addField(field)
            .build()
        );

        return builder.build();
    }

    public static Message formatPollComplete(Guild guild, Poll poll)
    {
        MessageBuilder builder = new MessageBuilder();

        builder.setContent(POLL_EMOJI+ "  **Poll '" + poll.getTitle() + "` Results!**  " + POLL_EMOJI);

        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for ( PollItems pollItem : poll.getPollitems() )
        {
            stringBuilder.append(pollItem.getReaction()).append(". `").append(pollItem.getItemName().replaceAll("_", " ")).append("` : [")
                .append(pollItem.getVotes()).append(" votes]\n");
            i++;
        }

        MessageEmbed.Field field = getPollWinner(poll.getPollitems());

        builder.setEmbed(new EmbedBuilder()
            .setColor(guild.getSelfMember().getColor())
            .setDescription(stringBuilder.toString())
            .addField(field)
            .build()
        );

        return builder.build();
    }

    public static Message formatPollComplete(Poll poll)
    {
        MessageBuilder builder = new MessageBuilder();

        builder.setContent(POLL_EMOJI+ "  **Poll '" + poll.getTitle() + "` Results!**  " + POLL_EMOJI);

        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for ( PollItems pollItem : poll.getPollitems() )
        {
            stringBuilder.append(pollItem.getReaction()).append(". `").append(pollItem.getItemName().replaceAll("_", " ")).append("` : [")
                .append(pollItem.getVotes()).append(" votes]\n");
            i++;
        }

        MessageEmbed.Field field = getPollWinner(poll.getPollitems());

        builder.setEmbed(new EmbedBuilder()
            .setColor(Color.CYAN)
            .setDescription(stringBuilder.toString())
            .addField(field)
            .build()
        );

        return builder.build();
    }

    public static String formatPoll(Poll poll)
    {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for ( PollItems pollItem : poll.getPollitems() )
        {
            stringBuilder.append(i).append(". `").append(pollItem.getItemName().replaceAll("_", " ")).append("` : [")
                .append(pollItem.getVotes()).append(" votes]\n");
            i++;
        }

        return stringBuilder.toString();
    }

    public static String formatPollItems(Set<PollItems> pollItems)
    {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for ( PollItems pollItem : pollItems )
        {
            stringBuilder.append(pollItem.getReaction()).append(". `").append(pollItem.getItemName().replaceAll("_", " ")).append("` : [")
                .append(pollItem.getVotes()).append(" votes]\n");
            i++;
        }

        return stringBuilder.toString();
    }

    public static String formatPollCompleteDirect(JDA jda, Poll poll)
    {
        return "Hi, `" + jda.getGuildById(poll.getGuildId()).getOwner().getUser().getName() + "`! " +
            "The Poll `" + poll.getTitle() + "` has concluded. Unfortunately no TextChannel was set to" +
            " display the results and it looks like there was an error sending this message to your \"default channel\". " +
            "When ready, please feel free to display the results of this poll in any TextChannel in your server!";
    }

    public static Message formatWelcomeMessage(WelcomeMessage message, Guild guild, User user)
    {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(message.getBody());
        builder.setFooter(message.getFooter(), null);
        builder.setThumbnail( message.getLogoUrl().isEmpty() ? guild.getIconUrl() : message.getLogoUrl() );

        if ( !message.getWebsiteUrl().equals("") )
            builder.addField("", message.getWebsiteUrl(), false);

        return new MessageBuilder()
            .setContent(message.getMessageTitle() + " **" + user.getAsMention()+ "**")
            .setEmbed(builder.build())
            .build();
    }
    public static Message formatWelcomeMessage(WelcomeMessage message, GuildMemberJoinEvent event)
    {
        return formatWelcomeMessage(message, event.getGuild(), event.getUser());
    }

    public static Message formatCustomCommand(CustomCommand customCommand)
    {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(customCommand.getMessage());

        return new MessageBuilder()
            .setEmbed(builder.build())
            .build();
    }

    public static Message formatGuildEvent(Guild guild, GuildEvent guildEvent, boolean started, boolean sample)
    {

        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();

        if ( started )
            if ( !sample )
                messageBuilder.setContent("@everyone \n" + guildEvent.getEventName()
                    .replaceAll("_", " ") + " Has started!!!!" );
            else
                messageBuilder.setContent(guildEvent.getEventName()
                    .replaceAll("_", " ") + " Has started!!!!" );
        else {
            Duration diff = Duration.between(Instant.now(), guildEvent.getEventStart());
            String timeUntil;

            if ( diff.toDays() != 0 )
                timeUntil = String.format("%d Day(s)", diff.toDays());
            if ( diff.toHours() != 0 )
                timeUntil = String.format("%d Hours %02d Minutes",
                    diff.toHours(),
                    (Math.abs(diff.getSeconds()) % 3600) / 60);
            else
                timeUntil = String.format("%02d Minutes", diff.toMinutes());

            if ( !sample )
                messageBuilder.setContent("@everyone \nREMINDER: `" + guildEvent.getEventName()
                    .replaceAll("_", " ") + "` Begins in " + timeUntil);
            else
                messageBuilder.setContent("REMINDER: `" + guildEvent.getEventName()
                    .replaceAll("_", " ") + "` Begins in " + timeUntil);
        }

        builder.setDescription(guildEvent.getEventMessage());
        builder.setThumbnail(guildEvent.getEventImageUrl().equals("") ? guild.getIconUrl() : guildEvent.getEventImageUrl());

        if ( !started )
            builder.setFooter("Start Time: " + OtherUtil.formatDateTime(guild, guildEvent.getEventStart()), null);

        messageBuilder.setEmbed(builder.build());
        return messageBuilder.build();
    }

    public static Message formatGiveAwayMessage(GiveAway giveAway, Guild guild, Boolean isLive)
    {
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();

        if ( isLive )
            messageBuilder.setContent("@everyone \n" + "There is a new Give Away! React with " + Constants.PARTY_EMOJI + " to enter!" );
        else
            messageBuilder.setContent("There is a new Give Away! React with " + Constants.PARTY_EMOJI + " to enter!" );

        builder.setTitle(giveAway.getName().replaceAll("_", " "));
        builder.setDescription(giveAway.getMessage());
        builder.setFooter("End Time: " + OtherUtil.formatDateTime(guild, giveAway.getFinish()), null);

        messageBuilder.setEmbed(builder.build());

        return messageBuilder.build();
    }

    public static Message formatGiveAwayWinner(GiveAway giveAway, User user)
    {
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();

        messageBuilder.setContent( Constants.PARTY_EMOJI + " @everyone " + Constants.PARTY_EMOJI);

        builder.setTitle(giveAway.getName().replaceAll("_", " ") + " Has ended");
        builder.setDescription("The winner is **" + user.getAsMention() + "**! Congratulations!");

        messageBuilder.setEmbed(builder.build());

        return messageBuilder.build();
    }

    public static Message formatNoGiveAwayWinner(GiveAway giveAway)
    {
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();

        messageBuilder.setContent( "\uD83D\uDE1E " + "@everyone" + " \uD83D\uDE1E");

        builder.setTitle(giveAway.getName().replaceAll("_", " ") + " Has ended");
        builder.setDescription("No user(s) entered the giveaway, so no winner has been declared");

        messageBuilder.setEmbed(builder.build());

        return messageBuilder.build();
    }

}
