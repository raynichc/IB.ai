/*******************************************************************************
 * Copyright 2018 pants
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.ibdiscord.data.db;

import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * @author pants
 * @since 2018.08.21
 */

public class DatabaseContainer {

    private final Morphia morphia = new Morphia();
    private Datastore database;

    public DatabaseContainer() {

    }

    public void connect(String mongoUsername,
                        String mongoPort,
                        String mongoPassword) {

        // Declaring map for Morphia to find entity classes
        morphia.mapPackage("com.ibdiscord.data.db.entities");

        // Tells the Morphia mapper to store null and empty values in Mongo
        morphia.getMapper().getOptions().setStoreNulls(true);
        morphia.getMapper().getOptions().setStoreEmpties(true);

        // setting the main database
        database = morphia.createDatastore(new MongoClient(), "main");
    }
}