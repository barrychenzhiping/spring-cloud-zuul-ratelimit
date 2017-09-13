/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config;

import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.Type;

import org.springframework.cloud.netflix.zuul.filters.Route;

import java.util.List;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import static com.google.common.net.HttpHeaders.X_FORWARDED_FOR;


@RequiredArgsConstructor
public class DefaultRateLimitKeyGenerator implements RateLimitKeyGenerator {

    private static final String ANONYMOUS_USER = "anonymous";

    private final RateLimitProperties properties;

    @Override
    public String key(final HttpServletRequest request, final Route route, final RateLimitProperties.Policy policy) {
        final List<Type> types = policy.getType();
        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(properties.getKeyPrefix());
        joiner.add(route.getId());
        if (!types.isEmpty()) {
            if (types.contains(Type.URL)) {
                joiner.add(route.getPath());
            }
            if (types.contains(Type.ORIGIN)) {
                joiner.add(getRemoteAddr(request));
            }
            if (types.contains(Type.USER)) {
                joiner.add(request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : ANONYMOUS_USER);
            }
        }
        return joiner.toString();
    }

    private String getRemoteAddr(final HttpServletRequest request) {
        if (properties.isBehindProxy() && request.getHeader(X_FORWARDED_FOR) != null) {
            return request.getHeader(X_FORWARDED_FOR);
        }
        return request.getRemoteAddr();
    }
}
