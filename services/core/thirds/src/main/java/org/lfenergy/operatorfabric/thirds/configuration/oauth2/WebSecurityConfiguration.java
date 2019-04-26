/* Copyright (c) 2018, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.lfenergy.operatorfabric.thirds.configuration.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * OAuth 2 http authentication configuration and access rules
 *
 * @author Alexandra Guironnet
 */
@Configuration
@Slf4j
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private Converter<Jwt, AbstractAuthenticationToken> opfabJwtConverter;

    private static final String[] AUTH_WHITELIST = {

            // -- swagger ui
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/swagger.json",
            "/v2/api-docs",
            "/webjars/**"
    };

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(AUTH_WHITELIST).permitAll()
                .antMatchers(HttpMethod.POST,"/thirds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT,"/thirds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE,"/thirds/**").hasRole("ADMIN")
                .anyRequest().permitAll() //Needed to allow styles to be loaded without auth
                .and()
                .oauth2ResourceServer()
                .jwt().jwtAuthenticationConverter(opfabJwtConverter);
    }


}
