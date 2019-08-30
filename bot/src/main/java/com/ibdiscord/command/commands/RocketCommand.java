/**
 * Copyright 2017-2019 Jarred Vardy
 * <p>
 * This file is part of IB.ai.
 * <p>
 * IB.ai is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * IB.ai is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with IB.ai. If not, see http://www.gnu.org/licenses/.
 */

package com.ibdiscord.command.commands;

import com.ibdiscord.command.Command;
import com.ibdiscord.command.CommandContext;
import com.ibdiscord.command.permissions.CommandPermission;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import de.arraying.kotys.*;

public final class RocketCommand extends Command {

    /**
     * Creates a new dad joke command.
     */
    public RocketCommand() {
        super("rocket",
                Set.of("nextlaunch"),
                CommandPermission.discord(),
                Set.of());
    }

    @Override
    protected void execute(CommandContext context) {
        try {
            URL url = new URL("https://launchlibrary.net/1.4/launch/next/" + context.getArguments()[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "text/plain");
            connection.setRequestProperty("Content-length", "0");
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.connect();
            int status = connection.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    JSON json = new JSON(sb.toString());
                    JSONArray launches = json.array("launches");
                    for (int i = 0; i < json.integer("count"); i++) {
                            JSON launch = launches.json(i);
                        context.reply(launch.string("name"));
                        context.reply(launch.string("windowstart"));
                    }
            }

        } catch(Exception ex) {
            context.reply("Something went wrong...");
            ex.printStackTrace();
        }
    }
}
