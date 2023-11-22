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
package net.pistonmaster.serverwrecker.auth.service;

import net.pistonmaster.serverwrecker.auth.AuthType;
import net.pistonmaster.serverwrecker.auth.HttpHelper;
import net.pistonmaster.serverwrecker.auth.MinecraftAccount;
import net.pistonmaster.serverwrecker.proxy.SWProxy;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.msa.StepCredentialsMsaCode;

import java.io.IOException;

public class SWJavaMicrosoftAuthService implements MCAuthService {
    public MinecraftAccount login(String email, String password, SWProxy proxyData) throws IOException {
        try (var httpClient = HttpHelper.createMCAuthHttpClient(proxyData)) {
            var fullJavaSession = MinecraftAuth.JAVA_CREDENTIALS_LOGIN.getFromInput(httpClient,
                    new StepCredentialsMsaCode.MsaCredentials(email, password));
            var mcProfile = fullJavaSession.getMcProfile();
            var mcToken = mcProfile.getMcToken();
            return new MinecraftAccount(AuthType.MICROSOFT_JAVA, mcProfile.getName(), new JavaData(mcProfile.getId(), mcToken.getAccessToken(), mcToken.getExpireTimeMs()), true);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
