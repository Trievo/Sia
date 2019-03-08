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
package com.trievosoftware.discord.automod;

import com.trievosoftware.discord.utils.FixedCache;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Invite;

/**
 *
 * @author Mark Tripoli (mark.tripoli@trievosoftware.com)
 */
public class InviteResolver
{
    private final FixedCache<String,Long> cached = new FixedCache<>(200);
    
    public long resolve(JDA jda, String code)
    {
        if(cached.contains(code))
            return cached.get(code);
        try
        {
            Invite i = Invite.resolve(jda, code).complete();
            cached.put(code, i.getGuild().getIdLong());
            return i.getGuild().getIdLong();
        }
        catch(Exception ex)
        {
            cached.put(code, 0L);
            return 0L;
        }
    }
}
