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
package net.pistonmaster.serverwrecker.account;

import net.pistonmaster.serverwrecker.proxy.SWProxy;
import net.raphimc.minecraftauth.util.MicrosoftConstants;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.List;

public class HttpHelper {
    private HttpHelper() {
    }

    public static CloseableHttpClient createMCAuthHttpClient(SWProxy proxyData) {
        return createHttpClient(MicrosoftConstants.getDefaultHeaders(), proxyData);
    }

    public static CloseableHttpClient createHttpClient(List<Header> headers, SWProxy proxyData) {
        var httpBuilder = HttpClientBuilder.create()
                .setDefaultHeaders(headers);

        var timeout = 5;
        var requestBuilder = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000);

        if (proxyData != null) {
            var proxy = new HttpHost(proxyData.host(), proxyData.port());

            if (proxyData.hasCredentials()) {
                var credentials = new UsernamePasswordCredentials(proxyData.username(), proxyData.password());

                var authScope = new AuthScope(proxy);

                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(authScope, credentials);

                httpBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }

            requestBuilder.setProxy(proxy);
        }

        httpBuilder.setDefaultRequestConfig(requestBuilder.build());

        return httpBuilder.build();
    }
}
