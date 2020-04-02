/* Copyright (c) 2020, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */


package org.lfenergy.operatorfabric.users.configuration.oauth2;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.operatorfabric.springtools.configuration.oauth.OAuth2GenericConfiguration;
import org.lfenergy.operatorfabric.springtools.configuration.oauth.OAuth2JwtProcessingUtilities;
import org.lfenergy.operatorfabric.springtools.configuration.oauth.OpFabJwtAuthenticationToken;
import org.lfenergy.operatorfabric.springtools.configuration.oauth.jwt.JwtProperties;
import org.lfenergy.operatorfabric.springtools.configuration.oauth.jwt.groups.GroupsMode;
import org.lfenergy.operatorfabric.springtools.configuration.oauth.jwt.groups.GroupsProperties;
import org.lfenergy.operatorfabric.springtools.configuration.oauth.jwt.groups.GroupsUtils;
import org.lfenergy.operatorfabric.users.model.User;
import org.lfenergy.operatorfabric.users.model.UserData;
import org.lfenergy.operatorfabric.users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

/**
 * Specific authentication configuration for the Users service It is necessary
 * because when converting the jwt token the {@link OAuth2GenericConfiguration}
 * relies on a user service cache and proxy to get user details, which is
 * useless in the case of the Users Service itself. Token conversion was made
 * necessary because access depending on roles is now used in the
 * WebSecurityConfiguration of this service.
 */
@Configuration
@Import({GroupsProperties.class, GroupsUtils.class, JwtProperties.class})
@Slf4j
@Data
public class OAuth2UsersConfiguration {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private GroupsProperties groupsProperties;

    @Autowired
    private GroupsUtils groupsUtils;

    /**
     * Generates a converter that converts {@link Jwt} to
     * {@link OpFabJwtAuthenticationToken} whose principal is a {@link User} model
     * object
     *
     * @return Converter from {@link Jwt} to {@link OpFabJwtAuthenticationToken}
     */
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> opfabJwtConverter(@Autowired UserRepository userRepository) {

        return new Converter<Jwt, AbstractAuthenticationToken>() {

            @Override
            public AbstractAuthenticationToken convert(Jwt jwt) {
                String principalId = jwt.getClaimAsString(jwtProperties.getLoginClaim());
                log.debug("USER {}  with the token : \n{}", principalId, jwt.getTokenValue());

                OAuth2JwtProcessingUtilities.token.set(jwt);

                Optional<UserData> optionalUser = userRepository.findById(principalId);

                UserData user;
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                } else {
                    user = createUserDataVirtualFromJwt(jwt);
                    log.warn("user virtual(nonexistent in opfab) : '{}'", user.toString());
                }

                OAuth2JwtProcessingUtilities.token.remove();

                if (groupsProperties.getMode() == GroupsMode.JWT) {
                    // override the groups list from JWT mode, otherwise, default mode is OPERATOR_FABRIC
                    user.setGroups(getGroupsList(jwt));
                }
                List<GrantedAuthority> authorities = computeAuthorities(user);

                log.debug("user [{}] has these roles {} through the {} mode"
                        , principalId, authorities.toString(), groupsProperties.getMode());

                return new OpFabJwtAuthenticationToken(jwt, user, authorities);
            }


            /**
             * create a temporal User from the jwt information without any group
             * @param jwt jwt
             * @return UserData
             */
            private UserData createUserDataVirtualFromJwt(Jwt jwt) {
                String principalId = extractClaimAsStringOrNull(jwt, jwtProperties.getLoginClaim());
                String givenName = extractClaimAsStringOrNull(jwt, jwtProperties.getGivenNameClaim());
                String familyName = extractClaimAsStringOrNull(jwt, jwtProperties.getFamilyNameClaim());
                return new UserData(principalId, givenName, familyName, null);
            }
        };
    }

    /**
     * needed otherwise raised geClaimAsString an NPE
     */
    private String extractClaimAsStringOrNull(Jwt jwt, String claim) {
        if (claim == null) return null;
        return jwt.getClaimAsString(claim);
    }

    /**
     * Creates Authority list from user's groups (ROLE_[group name])
     *
     * @param user user model data
     * @return list of authority
     */
    public static List<GrantedAuthority> computeAuthorities(User user) {
        return AuthorityUtils
                .createAuthorityList(user.getGroups().stream().map(g -> "ROLE_" + g).toArray(size -> new String[size]));
    }


    /**
     * Creates group list from a jwt
     *
     * @param jwt user's token
     * @return a group list
     */
    public List<String> getGroupsList(Jwt jwt) {
        return groupsUtils.createGroupsList(jwt);
    }
}
