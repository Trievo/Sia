/*
 * Copyright 2018 John Grosh (jagrosh).
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
package com.trievosoftware.discord;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.*;
import com.trievosoftware.discord.automod.AutoMod;
import com.trievosoftware.discord.automod.StrikeHandler;
import com.trievosoftware.discord.commands.CommandExceptionListener;
import com.trievosoftware.discord.commands.automod.*;
import com.trievosoftware.discord.commands.general.*;
import com.trievosoftware.discord.commands.moderation.*;
import com.trievosoftware.discord.commands.owner.*;
import com.trievosoftware.discord.commands.settings.*;
import com.trievosoftware.discord.commands.tools.*;
import com.trievosoftware.discord.commands.tools.LookupCmd;
import com.trievosoftware.discord.database.Database;
import com.trievosoftware.discord.logging.BasicLogger;
import com.trievosoftware.discord.logging.MessageCache;
import com.trievosoftware.discord.logging.ModLogger;
import com.trievosoftware.discord.logging.TextUploader;
import com.trievosoftware.discord.utils.BlockingSessionController;
import com.trievosoftware.discord.utils.FormatUtil;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.utils.cache.CacheFlag;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class Sia
{
    private final EventWaiter waiter;
    private final ScheduledExecutorService threadpool;
    private final Database database;
    private final TextUploader uploader;
    private final ShardManager shards;
    private final ModLogger modlog;
    private final BasicLogger basiclog;
    private final MessageCache messages;
    private final WebhookClient logwebhook;
    private final AutoMod automod;
    private final StrikeHandler strikehandler;
    
    public Sia() throws Exception
    {
        /**
         * Tokens:
         * 0  - bot token
         * 1  - bots.discord.pw token
         * 2  - other token 1 (unused)
         * 3  - other token 2 (unused)
         * 4  - database location
         * 5  - database username
         * 6  - database password
         * 7  - log webhook url
         * 8  - guild id : category id
         * 9  - number of shards
         * 10 - url resolver url
         * 11 - url resolver secret
         */
        List<String> tokens = Files.readAllLines(Paths.get("config.txt"));
        waiter = new EventWaiter(Executors.newSingleThreadScheduledExecutor(), false);
        threadpool = Executors.newScheduledThreadPool(50);
        database = new Database(tokens.get(4), tokens.get(5), tokens.get(6));
        String[] split = tokens.get(8).split(":");
        uploader = new TextUploader(this, Long.parseLong(split[0].trim()), Long.parseLong(split[1].trim()));
        modlog = new ModLogger(this);
        basiclog = new BasicLogger(this);
        messages = new MessageCache();
        logwebhook = new WebhookClientBuilder(tokens.get(7)).build();
        automod = new AutoMod(this, tokens);
        strikehandler = new StrikeHandler(this);
        CommandClient client = new CommandClientBuilder()
                        .setPrefix(Constants.PREFIX)
                        //.setGame(Game.watching("Type "+Constants.PREFIX+"help"))
                        .setGame(Game.playing(Constants.Wiki.SHORT_WIKI))
                        .setOwnerId(Constants.OWNER_ID)
                        .setServerInvite(Constants.SERVER_INVITE)
                        .setEmojis(Constants.SUCCESS, Constants.WARNING, Constants.ERROR)
                        .setLinkedCacheSize(0)
                        .setGuildSettingsManager(database.settings)
                        .setListener(new CommandExceptionListener())
                        .setScheduleExecutor(threadpool)
//                        .setShutdownAutomatically(false)
                        .addCommands(// General
                            //new AboutCommand(Color.CYAN, "and I'm here to keep your Discord server safe and make moderating easy!", 
                            //                            new String[]{"Moderation commands","Configurable automoderation","Very easy setup"},Constants.PERMISSIONS),
                            new AboutCmd(),
                            new InviteCmd(),
                            new PingCommand(),
                            new RoleinfoCmd(),
                            new ServerinfoCmd(),
                            new UserinfoCmd(),

                            // Moderation
                            new KickCmd(this),
                            new BanCmd(this),
                            new SoftbanCmd(this),
                            new UnbanCmd(this),
                            new CleanCmd(this),
                            new VoicemoveCmd(this),
                            new VoicekickCmd(this),
                            new MuteCmd(this),
                            new UnmuteCmd(this),
                            new RaidCmd(this),
                            new StrikeCmd(this),
                            new PardonCmd(this),
                            new CheckCmd(this),
                            new ReasonCmd(this),

                            // Settings
                            new SetupCmd(this),
                            new PunishmentCmd(this),
                            new MessagelogCmd(this),
                            new ModlogCmd(this),
                            new ServerlogCmd(this),
                            new VoicelogCmd(this),
                            new AvatarlogCmd(this),
                            new TimezoneCmd(this),
                            new ModroleCmd(this),
                            new PrefixCmd(this),
                            new SettingsCmd(this),

                            // Automoderation
                            new AntiinviteCmd(this),
                            new AnticopypastaCmd(this),
                            new AntieveryoneCmd(this),
                            new AntirefCmd(this),
                            new MaxlinesCmd(this),
                            new MaxmentionsCmd(this),
                            new AntiduplicateCmd(this),
                            new AutodehoistCmd(this),
                            new ResolvelinksCmd(this),
                            new AutoraidmodeCmd(this),
                            new IgnoreCmd(this),
                            new UnignoreCmd(this),
                            
                            // Tools
                            new AnnounceCmd(),
                            new AuditCmd(),
                            new DehoistCmd(),
                            new InvitepruneCmd(this),
                            new LookupCmd(this),

                            // Owner
                            new EvalCmd(this),
                            new DebugCmd(this),
                            new ReloadCmd(this),
                            new TransferCmd(this)
                        )
                        .setHelpConsumer(event -> event.replyInDm(FormatUtil.formatHelp(event, this), m -> 
                        {
                            if(event.isFromType(ChannelType.TEXT))
                                try
                                {
                                    event.getMessage().addReaction(Constants.HELP_REACTION).queue(s->{}, f->{});
                                } catch(PermissionException ignore) {}
                        }, t -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages.")))
                        .setDiscordBotsKey(tokens.get(1))
                        //.setCarbonitexKey(tokens.get(2))
                        //.setDiscordBotListKey(tokens.get(3))
                        .build();
        shards = new DefaultShardManagerBuilder()
                .setShardsTotal(Integer.parseInt(tokens.get(9)))
                .setToken(tokens.get(0))
                .addEventListeners(new Listener(this), client, waiter)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setGame(Game.playing("loading..."))
                .setBulkDeleteSplittingEnabled(false)
                .setRequestTimeoutRetry(true)
                .setDisabledCacheFlags(EnumSet.of(CacheFlag.EMOTE, CacheFlag.GAME)) //TODO: dont disable GAME
                .setSessionController(new BlockingSessionController())
                .build();
        
        modlog.start();
        
        threadpool.scheduleWithFixedDelay(() -> cleanPremium(), 0, 2, TimeUnit.HOURS);
        threadpool.scheduleWithFixedDelay(() -> leavePointlessGuilds(), 5, 30, TimeUnit.MINUTES);
        threadpool.scheduleWithFixedDelay(() -> System.gc(), 12, 6, TimeUnit.HOURS);
    }
    
    public EventWaiter getEventWaiter()
    {
        return waiter;
    }
    
    public Database getDatabase()
    {
        return database;
    }
    
    public ScheduledExecutorService getThreadpool()
    {
        return threadpool;
    }
    
    public TextUploader getTextUploader()
    {
        return uploader;
    }
    
    public ShardManager getShardManager()
    {
        return shards;
    }
    
    public ModLogger getModLogger()
    {
        return modlog;
    }
    
    public BasicLogger getBasicLogger()
    {
        return basiclog;
    }
    
    public MessageCache getMessageCache()
    {
        return messages;
    }
    
    public WebhookClient getLogWebhook()
    {
        return logwebhook;
    }
    
    public AutoMod getAutoMod()
    {
        return automod;
    }
    
    public StrikeHandler getStrikeHandler()
    {
        return strikehandler;
    }
    
    public void cleanPremium()
    {
        database.premium.cleanPremiumList().forEach((gid) ->
        {
            database.automod.setResolveUrls(gid, false);
            database.settings.setAvatarLogChannel(gid, null);
        });
    }
    
    public void leavePointlessGuilds()
    {
        shards.getGuilds().stream().filter(g -> 
        {
            if(!g.isAvailable())
                return false;
            int botcount = (int)g.getMemberCache().stream().filter(m -> m.getUser().isBot()).count();
            if(g.getMemberCache().size()-botcount<15 || (botcount>20 && ((double)botcount/g.getMemberCache().size())>0.65))
            {
                if(database.settings.hasSettings(g))
                    return false;
                if(database.automod.hasSettings(g))
                    return false;
                return true;
            }
            return false;
        }).forEach(g -> g.leave().queue());
    }
    
//    /**
//     * @param args the command line arguments
//     * @throws Exception
//     */
//    public static void main(String[] args) throws Exception
//    {
//        new Sia();
//    }
}
