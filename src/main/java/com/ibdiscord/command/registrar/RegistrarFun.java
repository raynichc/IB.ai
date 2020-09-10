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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibdiscord.animal.Animal;
import com.ibdiscord.command.actions.Odds;
import com.ibdiscord.command.registry.CommandRegistrar;
import com.ibdiscord.command.registry.CommandRegistry;
import com.ibdiscord.utils.UString;
import net.dv8tion.jda.api.entities.Message;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

public final class RegistrarFun implements CommandRegistrar {

    /**
     * Registers commands.
     * @param registry The command registry.
     */
    @Override
    public void register(CommandRegistry registry) {
        registry.define("coinflip")
                .on(context -> context.replyI18n(new Random().nextInt(2) == 0
                        ? "success.coinflip_heads"
                        : "success.coinflip_tails"
                ));

        registry.define("dadjoke")
                .on(context -> {
                    try {

                        URL url = new URL("https://icanhazdadjoke.com/");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestProperty("Accept", "text/plain");
                        connection.setRequestMethod("GET");

                        try(BufferedReader in = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()))) {

                            StringBuilder jokeBuilder = new StringBuilder();

                            String line;
                            while((line = in.readLine()) != null) {
                                jokeBuilder.append(line);
                                jokeBuilder.append(System.lineSeparator());
                            }

                            String response = jokeBuilder.toString();
                            context.replyRaw(response);
                        }

                    } catch(IOException exception) {
                        exception.printStackTrace();
                        context.replyI18n("error.generic");
                    }
                });

        registry.define("catpic")
                .on(context -> {
                    try {
                        URL url = new URL("https://api.thecatapi.com/v1/images/search");
                        OkHttpClient http = context.getJda().getHttpClient();
                        Request request = new Request.Builder().url(url).build();
                        Response response = http.newCall(request).execute();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<Animal>>() { }.getType();
                        List<Animal> catList = gson.fromJson(response.body().string(), listType);
                        context.replyRaw(catList.get(0).getUrl());
                    } catch(IOException exception) {
                        exception.printStackTrace();
                        context.replyI18n("error.generic");
                    }
                });

        registry.define("dogpic")
                .on(context -> {
                    try {
                        URL url = new URL("https://api.thedogapi.com/v1/images/search");
                        OkHttpClient http = context.getJda().getHttpClient();
                        Request request = new Request.Builder().url(url).build();
                        Response response = http.newCall(request).execute();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<Animal>>() { }.getType();
                        List<Animal> dogList = gson.fromJson(response.body().string(), listType);
                        context.replyRaw(dogList.get(0).getUrl());
                    } catch(IOException exception) {
                        exception.printStackTrace();
                        context.replyI18n("error.generic");
                    }
                });

        registry.define("odds")
                .on(new Odds());

        registry.define("roll")
                .on(context -> {
                    int number = 1;
                    int sides = 6;

                    if (context.getArguments().length > 0) {
                        try {
                            String[] dice = context.getArguments()[0].split("d");
                            number = Integer.parseInt(dice[0]);
                            sides = Integer.parseInt(dice[1]);

                            if (number < 1) {
                                number = 1;
                            }

                            if (sides < 1) {
                                sides = 1;
                            }
                        } catch (NumberFormatException e) {
                            number = 1;
                            sides = 6;
                        }
                    }

                    StringBuilder builder = new StringBuilder();
                    Random rand = new Random();

                    long sum = 0;
                    double var = 0;

                    for (int i = 0; i < number; i++) {
                        int value = rand.nextInt(sides) + 1;
                        sum += value;
                        var += value * value;
                        builder.append(value);
                        if (i < number - 1) {
                            builder.append(", ");
                        }
                    }

                    String roll = UString.truncate(builder.toString(), Message.MAX_CONTENT_LENGTH / 2);
                    if (context.getOptions().stream().anyMatch(it -> it.getName().equalsIgnoreCase("stats"))) {
                        double mean = ((double) sum) / number;
                        var = var / number - mean * mean;

                        context.replyI18n("success.roll_stats", roll, sum, String.format("%.3f", mean), String.format("%.3f", Math.sqrt(var)));
                    } else {
                        context.replyI18n("success.roll", roll);
                    }
                });
    }

}
