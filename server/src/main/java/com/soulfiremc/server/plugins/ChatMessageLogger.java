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
package com.soulfiremc.server.plugins;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.soulfiremc.server.api.InternalPlugin;
import com.soulfiremc.server.api.PluginInfo;
import com.soulfiremc.server.api.event.bot.ChatMessageReceiveEvent;
import com.soulfiremc.server.api.event.lifecycle.InstanceSettingsRegistryInitEvent;
import com.soulfiremc.server.settings.lib.InstanceSettingsSource;
import com.soulfiremc.server.settings.lib.SettingsObject;
import com.soulfiremc.server.settings.property.BooleanProperty;
import com.soulfiremc.server.settings.property.ImmutableBooleanProperty;
import com.soulfiremc.server.settings.property.ImmutableIntProperty;
import com.soulfiremc.server.settings.property.IntProperty;
import com.soulfiremc.server.util.SoulFireAdventure;
import com.soulfiremc.server.util.structs.SFLogAppender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.lenni0451.lambdaevents.EventHandler;
import org.pf4j.Extension;

import java.util.concurrent.TimeUnit;

@Slf4j
@Extension
public class ChatMessageLogger extends InternalPlugin {
  private static final Cache<String, Integer> CHAT_MESSAGES = Caffeine.newBuilder()
    .expireAfterWrite(5, TimeUnit.SECONDS)
    .build();

  public ChatMessageLogger() {
    super(new PluginInfo(
      "chat-message-logger",
      "1.0.0",
      "Logs all received chat messages to the terminal\nIncludes deduplication to prevent spamming the same message too often",
      "AlexProgrammerDE",
      "GPL-3.0",
      "https://soulfiremc.com"
    ));
  }

  @EventHandler
  public static void onMessage(ChatMessageReceiveEvent event) {
    var settingsSource = event.connection().settingsSource();
    if (!settingsSource.get(ChatMessageSettings.ENABLED)) {
      return;
    }

    // usage of synchronized method so that the chatMessages set is not modified while being
    // iterated
    logChatMessage(settingsSource, event.message());
  }

  private static synchronized void logChatMessage(InstanceSettingsSource settingsSource, Component message) {
    var ansiMessage = SoulFireAdventure.TRUE_COLOR_ANSI_SERIALIZER.serialize(message);

    var deduplicateAmount = settingsSource.get(ChatMessageSettings.DEDUPLICATE_AMOUNT);
    var messageCount = CHAT_MESSAGES.get(ansiMessage, (key) -> 0);
    if (messageCount < deduplicateAmount) {
      // Print to remote console (always true color)
      log.atInfo()
        .addKeyValue(SFLogAppender.SF_SKIP_LOCAL_APPENDERS, "true")
        .log("{}", ansiMessage);
      // Print to local console
      log.atInfo()
        .addKeyValue(SFLogAppender.SF_SKIP_PUBLISHING, "true")
        .log("{}", SoulFireAdventure.ANSI_SERIALIZER.serialize(message));
      CHAT_MESSAGES.put(ansiMessage, messageCount + 1);
    }
  }

  @EventHandler
  public void onSettingsRegistryInit(InstanceSettingsRegistryInitEvent event) {
    event.settingsRegistry().addClass(ChatMessageSettings.class, "Chat Message Logger", this, "logs");
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static class ChatMessageSettings implements SettingsObject {
    private static final String NAMESPACE = "chat-message-logger";
    public static final BooleanProperty ENABLED =
      ImmutableBooleanProperty.builder()
        .namespace(NAMESPACE)
        .key("enabled")
        .uiName("Log chat to terminal")
        .description("Log all received chat messages to the terminal")
        .defaultValue(true)
        .build();
    public static final IntProperty DEDUPLICATE_AMOUNT =
      ImmutableIntProperty.builder()
        .namespace(NAMESPACE)
        .key("deduplicate-amount")
        .uiName("Deduplicate amount")
        .description("How often should the same message be logged before it will not be logged again? (within 5 seconds)")
        .defaultValue(1)
        .minValue(1)
        .maxValue(Integer.MAX_VALUE)
        .stepValue(1)
        .build();
  }

  private record ChatMessageState(Cache<String, Integer> chatMessages) {}
}
