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
package net.pistonmaster.serverwrecker.pathfinding.execution;

import net.kyori.event.EventSubscriber;
import net.pistonmaster.serverwrecker.api.ServerWreckerAPI;
import net.pistonmaster.serverwrecker.api.event.bot.BotPreTickEvent;
import net.pistonmaster.serverwrecker.protocol.BotConnection;
import net.pistonmaster.serverwrecker.protocol.bot.BotMovementManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

public class PathExecutor implements EventSubscriber<BotPreTickEvent> {
    private final Queue<WorldAction> worldActions;
    private final BotConnection connection;
    private final Supplier<List<WorldAction>> findPath;

    public PathExecutor(BotConnection connection, List<WorldAction> worldActions, Supplier<List<WorldAction>> findPath) {
        this.worldActions = new ArrayBlockingQueue<>(worldActions.size());
        this.worldActions.addAll(worldActions);
        this.connection = connection;
        this.findPath = findPath;
    }

    @Override
    public void on(@NonNull BotPreTickEvent event) {
        BotConnection connection = event.connection();
        if (connection != this.connection) {
            return;
        }

        if (worldActions.isEmpty()) {
            unregister();
            return;
        }

        WorldAction worldAction = worldActions.peek();
        if (worldAction == null) {
            unregister();
            return;
        }

        if (worldAction instanceof RecalculatePathAction) {
            connection.logger().info("Recalculating path!");
            List<WorldAction> newActions = findPath.get();
            PathExecutor newExecutor = new PathExecutor(connection, newActions, findPath);
            ServerWreckerAPI.registerListener(BotPreTickEvent.class, newExecutor);
            unregister();
            return;
        }

        if (worldAction.isCompleted(connection)) {
            worldActions.remove();
            connection.logger().info("Reached goal! " + worldAction);

            // Directly use tick to execute next goal
            worldAction = worldActions.peek();

            // If there are no more goals, stop
            if (worldAction == null) {
                connection.logger().info("Finished all goals!");
                BotMovementManager movementManager = connection.sessionDataManager().getBotMovementManager();
                movementManager.getControlState().resetAll();
                unregister();
                return;
            }

            connection.logger().debug("Next goal: " + worldAction);
        }

        worldAction.tick(connection);
    }

    public void unregister() {
        ServerWreckerAPI.unregisterListener(this);
    }
}
