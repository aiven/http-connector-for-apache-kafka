/*
 * Copyright 2023 Aiven Oy and http-connector-for-apache-kafka project contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.aiven.kafka.connect.http.sender;

import java.net.ProxySelector;
import java.net.http.HttpClient;

import org.apache.kafka.connect.errors.ConnectException;

import io.aiven.kafka.connect.http.config.HttpSinkConfig;

public final class HttpSenderFactory {

    public static HttpSender createHttpSender(final HttpSinkConfig config) {
        final var clientBuilder = HttpClient.newBuilder();
        if (config.hasProxy()) {
            clientBuilder.proxy(ProxySelector.of(config.proxy()));
        }
        final var client = clientBuilder.build();
        switch (config.authorizationType()) {
            case NONE:
                return new DefaultHttpSender(config, client);
            case STATIC:
                return new StaticAuthHttpSender(config, client);
            case OAUTH2:
                final OAuth2AccessTokenHttpSender oauth2AccessTokenHttpSender =
                    new OAuth2AccessTokenHttpSender(config, client);
                return new OAuth2HttpSender(config, client, oauth2AccessTokenHttpSender);
            default:
                throw new ConnectException("Can't create HTTP sender for auth type: " + config.authorizationType());
        }
    }

}
