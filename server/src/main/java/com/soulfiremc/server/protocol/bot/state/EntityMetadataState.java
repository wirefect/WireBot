/*
 * SoulFire
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
package com.soulfiremc.server.protocol.bot.state;

import com.soulfiremc.server.data.NamedEntityData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataType;

import java.util.Optional;

@Data
public class EntityMetadataState {
  private final Int2ObjectMap<EntityMetadata<?, ?>> metadataStore = new Int2ObjectOpenHashMap<>();

  public void setMetadata(EntityMetadata<?, ?> metadata) {
    this.metadataStore.put(metadata.getId(), metadata);
  }

  public <T> Optional<T> getMetadata(NamedEntityData namedEntityData, MetadataType<T> ignored) {
    return getMetadata(namedEntityData.networkId(), ignored);
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> getMetadata(int id, MetadataType<T> ignored) {
    return (Optional<T>) Optional.ofNullable(this.metadataStore.get(id))
      .map(EntityMetadata::getValue);
  }
}
