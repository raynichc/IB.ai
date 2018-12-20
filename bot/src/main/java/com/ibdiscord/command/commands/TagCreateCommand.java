/**
 * Copyright 2018 raynichc
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author raynichc
 * @since 2018.11.29
 */

package com.ibdiscord.command.commands;

import com.ibdiscord.command.Command;
import com.ibdiscord.command.CommandContext;
import com.ibdiscord.command.permissions.CommandPermission;
import com.ibdiscord.data.db.entries.BotPrefixData;
import com.ibdiscord.data.db.entries.TagData;
import com.ibdiscord.main.IBai;

import net.dv8tion.jda.core.Permission;

import java.util.HashSet;

public final class TagCreateCommand extends Command {

    public TagCreateCommand() {
        super("create",
                new HashSet<String>(),
                CommandPermission.discord(Permission.MANAGE_CHANNEL),
                new HashSet<Command>());
    }

    @Override
    protected void execute(CommandContext context) {
        if(context.getArguments().length < 2) {
            String botPrefix = IBai.getConfig().getStaticPrefix();
            try {
                botPrefix = IBai.getDatabase().getGravity().load(new BotPrefixData(context.getGuild().getId())).get().toString();
            } catch(Exception e) {
            }
            context.reply("Correct usage: `" + botPrefix + "tag create \"[trigger]\" \"[output]\"`");
            return;
        }
        StringBuilder argumentBuilder = new StringBuilder();

        String trigger = "";
        String output = "";

        for(String arg : context.getArguments()) {
            if(argumentBuilder.length() == 0) {
                if (arg.startsWith("\"")) {
                    argumentBuilder.append(arg.substring(1));
                }
            } else {
                argumentBuilder.append(" ").append(arg);
            }

            if(arg.endsWith("\"")) {
                argumentBuilder.deleteCharAt(argumentBuilder.length()-1);
                if(trigger.isEmpty())
                    trigger = argumentBuilder.toString();
                else {
                    output = argumentBuilder.toString();
                    break;
                }
                argumentBuilder.delete(0, argumentBuilder.length());
            }
        }

        try {
            TagData tags = IBai.getDatabase().getGravity().load(new TagData(context.getGuild().getId()));
            tags.set(trigger, output);
            IBai.getDatabase().getGravity().save(tags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
