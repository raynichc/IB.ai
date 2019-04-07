package com.ibdiscord.command.commands;

import com.ibdiscord.command.Command;
import com.ibdiscord.command.CommandContext;
import com.ibdiscord.command.permissions.CommandPermission;
import com.ibdiscord.data.db.DContainer;
import com.ibdiscord.data.db.entries.GuildData;
import com.ibdiscord.data.db.entries.NoteData;
import com.ibdiscord.utils.UInput;
import com.ibdiscord.utils.UString;
import de.arraying.gravity.Gravity;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright 2019 Arraying
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
public final class NoteCommand extends Command {

    /**
     * Creates the command.
     */
    public NoteCommand() {
        super("note",
                Set.of("notes"),
                CommandPermission.role(GuildData.MODERATOR),
                new HashSet<>()
        );
        this.correctUsage = "note <user> [new note]";
    }

    @Override
    protected void execute(CommandContext context) {
        if(context.getArguments().length == 0) {
            context.reply("Please provide a user to add a note to.");
            return;
        }
        Member member = UInput.getMember(context.getGuild(), context.getArguments()[0]);
        if(member == null) {
            context.reply("Invalid member.");
            return;
        }
        Gravity gravity = DContainer.INSTANCE.getGravity();
        NoteData noteData = gravity.load(new NoteData(context.getGuild().getId(), member.getUser().getId()));
        if(context.getArguments().length == 1) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("Here are the notes for the user.");
            noteData.values().stream()
                    .map(it -> it.defaulting("N/A:N/A").toString())
                    .forEach(it -> {
                        String user;
                        String data;
                        int index = it.indexOf(":");
                        if(index < 0) {
                            user = "???";
                            data = it;
                        } else {
                            user = it.substring(0, index);
                            data = it.substring(index);
                            if(data.length() > 1) {
                                data = data.substring(1);
                            }
                        }
                        embedBuilder.addField("Entry by " + user + ":",
                                data,
                                false);
                    });
            context.reply(embedBuilder.build());
        } else {
            String note = UString.concat(context.getArguments(), " ", 1);
            if(note.length() > MessageEmbed.VALUE_MAX_LENGTH) {
                context.reply("Your note is too long! Please make it less than " + MessageEmbed.VALUE_MAX_LENGTH + " characters.");
                return;
            }
            if(noteData.size() >= 25) {
                context.reply("There are already too many notes on this user (Discord limitation).");
                return;
            }
            String data = context.getMember().getUser().getId() + ":" + note;
            noteData.add(data);
            gravity.save(noteData);
            context.reply("The note has been added.");
        }
    }

}