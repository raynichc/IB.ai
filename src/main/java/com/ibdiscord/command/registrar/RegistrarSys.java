/* Copyright 2017-2020 Arraying
 *
 * This file is part of IB.ai.
 *
 * IB.ai is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * IB.ai is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IB.ai. If not, see http://www.gnu.org/licenses/.
 */

package com.ibdiscord.command.registrar;

import com.ibdiscord.command.Command;
import com.ibdiscord.command.abstractions.React;
import com.ibdiscord.command.actions.*;
import com.ibdiscord.command.permission.CommandPermission;
import com.ibdiscord.command.registry.CommandRegistrar;
import com.ibdiscord.command.registry.CommandRegistry;
import com.ibdiscord.data.db.DataContainer;
import com.ibdiscord.data.db.entries.GuildData;
import com.ibdiscord.data.db.entries.react.EmoteData;
import com.ibdiscord.data.db.entries.react.ReactionData;
import com.ibdiscord.utils.UString;
import de.arraying.gravity.Gravity;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class RegistrarSys implements CommandRegistrar {

    /**
     * Registers commands.
     * @param registry The command registry.
     */
    @Override
    public void register(CommandRegistry registry) {
        Command commandCassowary = registry.define("cassowary")
                .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                .sub(registry.sub("create", "generic_create")
                        .on(new CassowaryCreate())
                )
                .sub(registry.sub("delete", "generic_delete")
                        .on(new CassowaryDelete())
                )
                .sub(registry.sub("list", "generic_list")
                        .on(new CassowaryList())
                );
        commandCassowary.on(context -> context.replySyntax(commandCassowary));

        registry.define("log")
                .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                .on(new Logging(GuildData.LOGS));

        registry.define("modlog")
                .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                .on(new Logging(GuildData.MODLOGS));

        registry.define("moderator")
                .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                .on(context -> {
                        Gravity gravity = DataContainer.INSTANCE.getGravity();
                        GuildData guildData = gravity.load(new GuildData(context.getGuild().getId()));
                        if(context.getArguments().length == 0) {
                            String permission = guildData.get(GuildData.MODERATOR)
                                    .defaulting("not set")
                                    .asString();
                            context.replyI18n("info.mod_permission", permission);
                            return;
                        }
                        String newValue = UString.concat(context.getArguments(), " ", 0);
                        guildData.set(GuildData.MODERATOR, newValue);
                        gravity.save(guildData);
                        context.replyI18n("success.mod_permission");
                });

        registry.define("muterole")
                .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                .on(context -> {
                        List<Role> roles = context.getMessage().getMentionedRoles();
                        if(roles.isEmpty()) {
                            context.replyI18n("error.mute_role");
                            return;
                        }
                        Gravity gravity = DataContainer.INSTANCE.getGravity();
                        GuildData guildData = gravity.load(new GuildData(context.getGuild().getId()));
                        guildData.set(GuildData.MUTE, roles.get(0).getId());
                        gravity.save(guildData);
                        context.replyI18n("success.mute_role");
                });

        registry.define("prefix")
                .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                .on(context -> {
                        context.assertArguments(1, "error.generic_arg_length");
                        String prefixNew = context.getArguments()[0];
                        if(Arrays.stream(new String[]{"/", "$", "#", "+", "*", "?"}).anyMatch(prefixNew::equals)) {
                            context.replyI18n("error.prefix", prefixNew);
                            return;
                        }

                        GuildData guildData = DataContainer.INSTANCE.getGravity().load(new GuildData(context.getGuild().getId()));
                        guildData.set(GuildData.PREFIX, prefixNew);
                        DataContainer.INSTANCE.getGravity().save(guildData);
                        context.replyI18n("success.prefix", prefixNew);
                });

        Command commandReact = registry.define("react")
                .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                .sub(registry.sub("create", "generic_create")
                        .on(new React() {
                            protected void modifyData(ReactionData data, String emote, List<String> roleIDs) {
                                String uniqueID = UUID.randomUUID().toString();
                                data.set(emote, uniqueID);
                                EmoteData emoteData = DataContainer.INSTANCE.getGravity().load(new EmoteData(uniqueID));
                                roleIDs.forEach(emoteData::add);
                                DataContainer.INSTANCE.getGravity().save(emoteData);
                            }

                            protected void modifyMessage(Message message, Object emote) {
                                if(emote instanceof Emote) {
                                    message.addReaction((Emote) emote).queue();
                                } else {
                                    message.addReaction(emote.toString()).queue();
                                }
                            }
                        })
                )
                .sub(registry.sub("delete", "generic_delete")
                        .on(new React() {
                            protected void modifyData(ReactionData data, String emote, List<String> roleIDs) {
                                DataContainer.INSTANCE.getGravity()
                                        .load(new EmoteData(data.get(emote).asString()))
                                        .delete();
                                data.unset(emote);
                            }

                            protected void modifyMessage(Message message, Object emote) {
                                // Do nothing. Thanks for making me add this comment, checkstyle.
                            }
                        })
                );
        commandReact.on(context -> context.replySyntax(commandReact));

        registry.define("roleswap")
                .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                .on(context -> {
                    List<Role> roles = context.getMessage().getMentionedRoles();
                    if(roles.isEmpty()) {
                        context.replyI18n("error.swap_empty");
                        return;
                    } else if(roles.size() == 1) {
                        context.replyI18n("error.swap_missing");
                        return;
                    }

                    List<Member> members = context.getGuild().getMembersWithRoles(roles.get(0));
                    members.forEach(member ->
                            context.getGuild().modifyMemberRoles(member,
                                    roles.subList(1,2),
                                    roles.subList(0,1)).queue());
                    context.replyI18n("success.swap_role", members.size());
                });

        Command commandTag = registry.define("tag") // Explicitly state it to allow cross referencing.
                .sub(registry.sub("activate", "tag_activate")
                        .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                        .on(new TagActivate())
                )
                .sub(registry.sub("create", "generic_create")
                        .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                        .on(new TagCreate())
                )
                .sub(registry.sub("delete", "generic_delete")
                        .restrict(CommandPermission.discord(Permission.MANAGE_SERVER))
                        .on(new TagDelete())
                )
                .sub(registry.sub("disabled", null)
                        .on(new TagDisabled())
                )
                .sub(registry.sub("find", "tag_find")
                        .on(new TagFind())
                )
                .sub(registry.sub("list", "generic_list")
                        .on(new TagList())
                );
        commandTag.on(context -> context.replySyntax(commandTag));
    }

}
