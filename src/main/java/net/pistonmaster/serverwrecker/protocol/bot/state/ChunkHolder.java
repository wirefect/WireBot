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
package net.pistonmaster.serverwrecker.protocol.bot.state;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.pistonmaster.serverwrecker.data.BlockType;
import net.pistonmaster.serverwrecker.data.ResourceData;
import net.pistonmaster.serverwrecker.protocol.bot.block.BlockStateMeta;
import net.pistonmaster.serverwrecker.protocol.bot.model.ChunkKey;
import net.pistonmaster.serverwrecker.util.NoopLock;
import org.cloudburstmc.math.vector.Vector3i;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChunkHolder {
    private final Int2ObjectMap<ChunkData> chunks = new Int2ObjectOpenHashMap<>();
    private final Lock readLock;
    private final Lock writeLock;
    private final LevelState levelState;

    public ChunkHolder(LevelState levelState) {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
        this.levelState = levelState;
    }

    private ChunkHolder(ChunkHolder chunkHolder) {
        this.levelState = chunkHolder.levelState;
        this.chunks.putAll(chunkHolder.chunks);
        this.readLock = new NoopLock();
        this.writeLock = new NoopLock();
    }

    public ChunkData getChunk(int x, int z) {
        readLock.lock();
        try {
            return chunks.get(ChunkKey.calculateHash(x, z));
        } finally {
            readLock.unlock();
        }
    }

    public ChunkData getChunk(Vector3i block) {
        readLock.lock();
        try {
            return chunks.get(ChunkKey.calculateHash(block));
        } finally {
            readLock.unlock();
        }
    }

    public boolean isChunkLoaded(int x, int z) {
        readLock.lock();
        try {
            return chunks.containsKey(ChunkKey.calculateHash(x, z));
        } finally {
            readLock.unlock();
        }
    }

    public boolean isChunkLoaded(Vector3i block) {
        readLock.lock();
        try {
            return chunks.containsKey(ChunkKey.calculateHash(block));
        } finally {
            readLock.unlock();
        }
    }

    public void removeChunk(int x, int z) {
        writeLock.lock();
        try {
            chunks.remove(ChunkKey.calculateHash(x, z));
        } finally {
            writeLock.unlock();
        }
    }

    public ChunkData getOrCreateChunk(int x, int z) {
        writeLock.lock();
        try {
            return chunks.computeIfAbsent(ChunkKey.calculateHash(x, z), (key) ->
                    new ChunkData(levelState));
        } finally {
            writeLock.unlock();
        }
    }

    public Optional<BlockStateMeta> getBlockStateAt(Vector3i block) {
        var chunkData = getChunk(block);

        // Out of world
        if (chunkData == null) {
            return Optional.empty();
        }

        return Optional.of(ResourceData.GLOBAL_BLOCK_PALETTE
                .getBlockStateForStateId(chunkData.getBlock(block)));
    }

    public Optional<BlockType> getBlockTypeAt(Vector3i block) {
        return getBlockStateAt(block).map(BlockStateMeta::blockType);
    }

    public ChunkHolder immutableCopy() {
        return new ChunkHolder(this);
    }
}