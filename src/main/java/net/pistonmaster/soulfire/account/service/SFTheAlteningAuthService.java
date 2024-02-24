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
package net.pistonmaster.soulfire.account.service;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.soulfire.account.AuthType;
import net.pistonmaster.soulfire.account.HttpHelper;
import net.pistonmaster.soulfire.account.MinecraftAccount;
import net.pistonmaster.soulfire.proxy.SFProxy;
import net.pistonmaster.soulfire.server.util.UUIDHelper;
import net.pistonmaster.soulfire.util.GsonInstance;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;

public final class SFTheAlteningAuthService
    implements MCAuthService<SFTheAlteningAuthService.TheAlteningAuthData> {
  @SuppressWarnings("HttpUrlsUsage") // The Altening doesn't support encrypted HTTPS
  private static final URI AUTHENTICATE_ENDPOINT =
      URI.create("http://authserver.thealtening.com/authenticate");

  private static final String PASSWORD =
      "SoulFireIsCool"; // Password doesn't matter for The Altening

  @Override
  public MinecraftAccount login(TheAlteningAuthData data, SFProxy proxyData) throws IOException {
    var request =
        new AuthenticationRequest(data.altToken, PASSWORD, UUID.randomUUID().toString());
    return HttpHelper.createReactorClient(null, true)
        .post()
        .uri(AUTHENTICATE_ENDPOINT)
        .send(ByteBufFlux.fromString(Flux.just(GsonInstance.GSON.toJson(request))))
        .responseSingle(
            (res, content) ->
                content
                    .asString()
                    .map(
                        responseText -> {
                          var response =
                              GsonInstance.GSON.fromJson(
                                  responseText,
                                  AuthenticateRefreshResponse.class);

                          return new MinecraftAccount(
                              AuthType.THE_ALTENING,
                              response.selectedProfile().name(),
                              new OnlineJavaData(
                                  UUIDHelper.convertToDashed(response.selectedProfile().id()),
                                  response.accessToken(),
                                  -1),
                              true);
                        }))
        .block();
  }

  @Override
  public TheAlteningAuthData createData(String data) {
    return new TheAlteningAuthData(data);
  }

  public record TheAlteningAuthData(String altToken) {}

  private record Agent(String name, int version) {}

  @SuppressWarnings("unused") // Used by GSON
  @RequiredArgsConstructor
  private static class AuthenticationRequest {
    private final Agent agent = new Agent("Minecraft", 1);
    private final String username;
    private final String password;
    private final String clientToken;
    private final boolean requestUser = true;
  }

  @SuppressWarnings("unused") // Used by GSON
  @Getter
  private static class AuthenticateRefreshResponse {
    private String accessToken;
    private ResponseGameProfile selectedProfile;
  }

  @SuppressWarnings("unused") // Used by GSON
  @Getter
  private static class ResponseGameProfile {
    private String id;
    private String name;
  }
}
