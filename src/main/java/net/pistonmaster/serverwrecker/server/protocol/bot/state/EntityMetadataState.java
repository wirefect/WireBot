/*
 * ServerWrecker
 * Copyright (C) 2024  AlexProgrammerDE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.pistonmaster.serverwrecker.server.protocol.bot.state;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;

import java.util.Map;

@Data
public class EntityMetadataState {
    private final Map<Integer, EntityMetadata<?, ?>> metadataStore = new Int2ObjectOpenHashMap<>();

    public void setMetadata(EntityMetadata<?, ?> metadata) {
        this.metadataStore.put(metadata.getId(), metadata);
    }
}
