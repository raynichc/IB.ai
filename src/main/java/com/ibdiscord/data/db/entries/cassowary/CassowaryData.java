/* Copyright 2018-2020 Jarred Vardy <vardy@riseup.net>
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

package com.ibdiscord.data.db.entries.cassowary;

import de.arraying.gravity.data.types.TypeSet;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class CassowaryData extends TypeSet {

    private final String guild;
    private final String label;

    @Override
    protected String getUniqueIdentifier() {
        return "cassowary_" + this.guild + "_" + this.label;
    }

}
