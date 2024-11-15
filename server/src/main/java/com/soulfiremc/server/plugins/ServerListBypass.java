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

import com.soulfiremc.server.api.InternalPlugin;
import com.soulfiremc.server.api.PluginInfo;
import com.soulfiremc.server.api.event.bot.PreBotConnectEvent;
import com.soulfiremc.server.api.event.lifecycle.InstanceSettingsRegistryInitEvent;
import com.soulfiremc.server.settings.lib.SettingsObject;
import com.soulfiremc.server.settings.property.BooleanProperty;
import com.soulfiremc.server.settings.property.ImmutableBooleanProperty;
import com.soulfiremc.server.settings.property.ImmutableMinMaxProperty;
import com.soulfiremc.server.settings.property.MinMaxProperty;
import com.soulfiremc.server.util.TimeUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.lenni0451.lambdaevents.EventHandler;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.pf4j.Extension;

import java.util.concurrent.TimeUnit;

@Extension
public class ServerListBypass extends InternalPlugin {
  public ServerListBypass() {
    super(new PluginInfo(
      "server-list-bypass",
      "1.0.0",
      "Bypasses server list anti-bots",
      "AlexProgrammerDE",
      "GPL-3.0"
    ));
  }

  @EventHandler
  public static void onPreConnect(PreBotConnectEvent event) {
    var connection = event.connection();
    if (connection.targetState() == ProtocolState.STATUS) {
      return;
    }

    var factory = connection.factory();
    var settingsSource = connection.settingsSource();
    if (!settingsSource.get(ServerListBypassSettings.ENABLED)) {
      return;
    }

    factory.prepareConnectionInternal(ProtocolState.STATUS).connect().join();
    TimeUtil.waitTime(
      settingsSource.getRandom(ServerListBypassSettings.DELAY).getAsLong(),
      TimeUnit.SECONDS);
  }

  @EventHandler
  public void onSettingsRegistryInit(InstanceSettingsRegistryInitEvent event) {
    event.settingsRegistry().addClass(ServerListBypassSettings.class, "Server List Bypass", this, "network");
  }

  @NoArgsConstructor(access = AccessLevel.NONE)
  private static class ServerListBypassSettings implements SettingsObject {
    private static final String NAMESPACE = "server-list-bypass";
    public static final BooleanProperty ENABLED =
      ImmutableBooleanProperty.builder()
        .namespace(NAMESPACE)
        .key("enabled")
        .uiName("Enable Server List Bypass")
        .description("Whether to ping the server list before connecting. (Bypasses anti-bots like EpicGuard)")
        .defaultValue(false)
        .build();
    public static final MinMaxProperty DELAY = ImmutableMinMaxProperty.builder()
      .namespace(NAMESPACE)
      .key("delay")
      .minUiName("Min delay (seconds)")
      .maxUiName("Max delay (seconds)")
      .minDescription("Minimum delay between joining the server")
      .maxDescription("Maximum delay between joining the server")
      .minDefaultValue(1)
      .maxDefaultValue(3)
      .minValue(0)
      .maxValue(Integer.MAX_VALUE)
      .stepValue(1)
      .build();
  }
}
