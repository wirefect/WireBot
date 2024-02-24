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
package net.pistonmaster.soulfire.account;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lenni0451.commons.httpclient.HttpClient;
import net.lenni0451.commons.httpclient.HttpResponse;
import net.lenni0451.commons.httpclient.executor.RequestExecutor;
import net.lenni0451.commons.httpclient.requests.HttpContentRequest;
import net.lenni0451.commons.httpclient.requests.HttpRequest;
import net.lenni0451.commons.httpclient.utils.HttpRequestUtils;
import net.lenni0451.commons.httpclient.utils.URLWrapper;
import net.pistonmaster.soulfire.builddata.BuildData;
import net.pistonmaster.soulfire.proxy.SFProxy;
import net.raphimc.minecraftauth.MinecraftAuth;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.transport.ProxyProvider;

@Slf4j
public class HttpHelper {
  private HttpHelper() {}

  public static reactor.netty.http.client.HttpClient createReactorClient(
      SFProxy proxyData, boolean withBody) {
    var base =
        reactor.netty.http.client.HttpClient.create()
            .responseTimeout(Duration.ofSeconds(5))
            .headers(
                h -> {
                  h.add("Accept", "application/json");
                  if (withBody) {
                    h.add("Content-Type", "application/json");
                  }

                  h.add("Accept-Language", "en-US,en");
                  h.add("User-Agent", "SoulFire/" + BuildData.VERSION);
                });

    return proxyData == null
        ? base
        : base.proxy(
            p -> {
              var spec =
                  p.type(
                          switch (proxyData.type()) {
                            case HTTP -> ProxyProvider.Proxy.HTTP;
                            case SOCKS4 -> ProxyProvider.Proxy.SOCKS4;
                            case SOCKS5 -> ProxyProvider.Proxy.SOCKS5;
                          })
                      .host(proxyData.host())
                      .port(proxyData.port())
                      .nonProxyHosts("localhost")
                      .connectTimeoutMillis(20_000);

              if (proxyData.username() != null) {
                spec.username(proxyData.username());
              }

              if (proxyData.password() != null) {
                spec.password(s -> proxyData.password());
              }
            });
  }

  public static HttpClient createLenniMCAuthHttpClient(SFProxy proxyData) {
    return MinecraftAuth.createHttpClient()
        .setExecutor(client -> new ReactorLenniExecutor(proxyData, client));
  }

  private static class ReactorLenniExecutor extends RequestExecutor {
    private final SFProxy proxyData;

    public ReactorLenniExecutor(SFProxy proxyData, HttpClient httpClient) {
      super(httpClient);
      this.proxyData = proxyData;
    }

    @NotNull
    @Override
    @SneakyThrows
    public HttpResponse execute(@NotNull HttpRequest httpRequest) {
      var cookieManager = getCookieManager(httpRequest);
      try {
        log.debug("Executing request: {}", httpRequest.getURL());
        var requestHeaders = getHeaders(httpRequest, cookieManager);

        var base =
            createReactorClient(proxyData, false)
                .followRedirect(
                    switch (httpRequest.getFollowRedirects()) {
                      case NOT_SET -> client.isFollowRedirects();
                      case FOLLOW -> true;
                      case IGNORE -> false;
                    })
                .responseTimeout(Duration.ofMillis(client.getReadTimeout()))
                .headers(h -> requestHeaders.forEach((k, v) -> h.set(k, String.join("; ", v))))
                .request(HttpMethod.valueOf(httpRequest.getMethod()))
                .uri(httpRequest.getURL().toURI());

        reactor.netty.http.client.HttpClient.ResponseReceiver<?> receiver;
        if (httpRequest instanceof HttpContentRequest contentRequest) {
          receiver =
              base.send(
                  ByteBufFlux.fromInbound(
                      Flux.just(Objects.requireNonNull(contentRequest.getContent()).getAsBytes())));
        } else {
          receiver = base;
        }

        return receiver
            .responseSingle(
                (res, content) -> {
                  try {
                    var code = res.status().code();
                    var url = new URLWrapper(Objects.requireNonNull(res.resourceUrl()));
                    var urlObj = url.toURL();
                    var responseHeaders = getAsMap(res.responseHeaders());

                    HttpRequestUtils.updateCookies(cookieManager, url.toURL(), responseHeaders);

                    return content
                        .asByteArray()
                        .mapNotNull(
                            bytes -> new HttpResponse(
                                urlObj, code, bytes, responseHeaders))
                        .switchIfEmpty(Mono.just(new HttpResponse(urlObj, code, null, responseHeaders)));
                  } catch (Exception e) {
                    log.error("Error while handling response", e);
                    return Mono.error(e);
                  }
                })
            .blockOptional()
            .orElseThrow();
      } catch (Exception e) {
        throw new IOException(e);
      }
    }
  }

  private static Map<String, List<String>> getAsMap(HttpHeaders headers) {
    return headers.entries().stream()
        .collect(
            Collectors.groupingBy(
                Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
  }
}
