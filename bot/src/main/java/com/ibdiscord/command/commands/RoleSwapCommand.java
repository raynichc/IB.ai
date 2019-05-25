package com.ibdiscord.command.commands;

import com.ibdiscord.command.Command;
import com.ibdiscord.command.CommandContext;
import com.ibdiscord.command.permissions.CommandPermission;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.Member;

import java.util.List;
import java.util.Set;

/**
 * Copyright 2017-2019 raynichc
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
public final class RoleSwapCommand extends Command {

    /**
     * Creates the command.
     */
    public RoleSwapCommand() {
        super("roleswap",
                Set.of("swaproles"),
                CommandPermission.discord(Permission.MANAGE_ROLES),
                Set.of()
        );
        this.correctUsage = "roleswap <target role> <new role>";
    }

    /**
     * Sets the mute role.
     * @param context The command context.
     */
    @Override
    protected void execute(CommandContext context) {
        List<Role> roles = context.getMessage().getMentionedRoles();
        if(roles.isEmpty()) {
            context.reply("Please mention the roles that you want to swap.");
            return;
        } else if (roles.size() == 1) {
            context.reply("Please mention a new role to swap to.");
            return;
        }

        List<Member> members = context.getGuild().getMembersWithRoles(roles.get(0));
        for (Member member : members) {
            context.getGuild().getController().modifyMemberRoles(member, roles.get(1), roles.get(0)).queue();
        }

        context.reply("The roles have been swapped for " + members.size() + " users.");
    }

}
