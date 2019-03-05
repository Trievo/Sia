/*
 * Copyright 2017 John Grosh (john.a.grosh@gmail.com).
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
package com.trievosoftware.discord.database;

import com.trievosoftware.discord.database.managers.*;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Database
{
//    public final AutomodManager automod; // automod settings
    public final GuildSettingsDataManager settings; // logs and other settings
    public final IgnoreManager ignores; // ignored roles and channels
//    public final AuditCacheManager auditcache; // cache of latest audit logs
    public final StrikeManager strikes; // strike counts for members
    public final PunishmentManager actions; // strike punishment settings
    public final TempMuteManager tempmutes;
    public final TempBanManager tempbans;
    public final PremiumManager premium;
    
    public Database(String host, String user, String pass) throws Exception
    {

//        automod = new AutomodManager();
        settings = new GuildSettingsDataManager();
        ignores = new IgnoreManager();
//        auditcache = new AuditCacheManager();
        strikes = new StrikeManager();
        actions = new PunishmentManager();
        tempmutes = new TempMuteManager();
        tempbans = new TempBanManager();
        premium = new PremiumManager();
        
    }
}