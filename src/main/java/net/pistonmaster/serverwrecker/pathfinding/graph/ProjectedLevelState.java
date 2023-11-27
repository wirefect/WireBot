/*
 * ServerWrecker
 *
 * Copyright (C) 2023 ServerWrecker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.pistonmaster.serverwrecker.pathfinding.graph;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.serverwrecker.data.BlockShapeType;
import net.pistonmaster.serverwrecker.data.BlockType;
import net.pistonmaster.serverwrecker.pathfinding.Costs;
import net.pistonmaster.serverwrecker.pathfinding.SWVec3i;
import net.pistonmaster.serverwrecker.protocol.bot.block.BlockStateMeta;
import net.pistonmaster.serverwrecker.protocol.bot.state.ChunkHolder;
import net.pistonmaster.serverwrecker.protocol.bot.state.LevelState;
import net.pistonmaster.serverwrecker.util.VectorHelper;

import java.util.Optional;

/**
 * An immutable representation of the world state.
 * This takes a world state and projects changes onto it.
 * This way we calculate the way we can do actions after a block was broken/placed.
 */
@RequiredArgsConstructor
public class ProjectedLevelState {
    private static final BlockStateMeta AIR_BLOCK_STATE = new BlockStateMeta(BlockType.AIR, BlockShapeType.getById(0));
    private static final BlockStateMeta VOID_AIR_BLOCK_STATE = new BlockStateMeta(BlockType.VOID_AIR, BlockShapeType.getById(0));

    private final ChunkHolder chunkHolder;
    private final Object2ObjectMap<SWVec3i, BlockStateMeta> blockChanges;
    private final int blockChangesHash;
    private final int minBuildHeight;
    private final int maxBuildHeight;

    public ProjectedLevelState(LevelState levelState) {
        this.chunkHolder = levelState.getChunks().immutableCopy();
        this.blockChanges = Object2ObjectMaps.emptyMap();
        this.blockChangesHash = blockChanges.hashCode();
        this.minBuildHeight = levelState.getMinBuildHeight();
        this.maxBuildHeight = levelState.getMaxBuildHeight();
    }

    public ProjectedLevelState withChangeToSolidBlock(SWVec3i position) {
        var blockChanges = new Object2ObjectOpenCustomHashMap<SWVec3i, BlockStateMeta>(
                this.blockChanges.size() + 1, VectorHelper.VECTOR3I_HASH_STRATEGY);
        blockChanges.putAll(this.blockChanges);
        blockChanges.put(position, Costs.SOLID_PLACED_BLOCK_STATE);

        return new ProjectedLevelState(chunkHolder, blockChanges, blockChanges.hashCode(), minBuildHeight, maxBuildHeight);
    }

    public ProjectedLevelState withChangeToAir(SWVec3i position) {
        var blockChanges = new Object2ObjectOpenCustomHashMap<SWVec3i, BlockStateMeta>(
                this.blockChanges.size() + 1, VectorHelper.VECTOR3I_HASH_STRATEGY);
        blockChanges.putAll(this.blockChanges);
        blockChanges.put(position, AIR_BLOCK_STATE);

        return new ProjectedLevelState(chunkHolder, blockChanges, blockChanges.hashCode(), minBuildHeight, maxBuildHeight);
    }

    public ProjectedLevelState withChanges(SWVec3i[] air, SWVec3i solid) {
        var blockChanges = new Object2ObjectOpenCustomHashMap<SWVec3i, BlockStateMeta>(
                this.blockChanges.size() + (air != null ? air.length : 0)
                        + (solid != null ? 1 : 0), VectorHelper.VECTOR3I_HASH_STRATEGY);
        blockChanges.putAll(this.blockChanges);

        if (air != null) {
            for (var position : air) {
                if (position == null) {
                    continue;
                }

                blockChanges.put(position, AIR_BLOCK_STATE);
            }
        }

        if (solid != null) {
            blockChanges.put(solid, Costs.SOLID_PLACED_BLOCK_STATE);
        }

        return new ProjectedLevelState(chunkHolder, blockChanges, blockChanges.hashCode(), minBuildHeight, maxBuildHeight);
    }

    public Optional<BlockStateMeta> getBlockStateAt(SWVec3i position) {
        // So that we don't throw OutOfLevelException when we are in the void,
        // OutOfLevelException should be only thrown when we are outside the render distance
        var y = position.y;
        if (y < minBuildHeight) {
            return Optional.of(VOID_AIR_BLOCK_STATE);
        } else if (y >= maxBuildHeight) {
            return Optional.of(VOID_AIR_BLOCK_STATE);
        }

        var blockChange = blockChanges.get(position);
        if (blockChange != null) {
            return Optional.of(blockChange);
        }

        return chunkHolder.getBlockStateAt(position.x, position.y, position.z);
    }

    public boolean isChanged(SWVec3i position) {
        return blockChanges.containsKey(position);
    }

    public boolean equals(ProjectedLevelState that) {
        return blockChanges.equals(that.blockChanges);
    }

    @Override
    public int hashCode() {
        return blockChangesHash;
    }
}
