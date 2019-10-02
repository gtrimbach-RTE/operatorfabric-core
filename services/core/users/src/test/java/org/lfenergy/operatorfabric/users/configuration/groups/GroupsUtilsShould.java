package org.lfenergy.operatorfabric.users.configuration.groups;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.operatorfabric.users.application.UnitTestApplication;
import org.lfenergy.operatorfabric.users.configuration.jwt.groups.GroupsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UnitTestApplication.class)
@ActiveProfiles(profiles = {"default", "test-init"})
@WebAppConfiguration
@Slf4j
public class GroupsUtilsShould {
	
	@Autowired
	private GroupsUtils groupsUtils;
	
	@Test
    public void createAuthorityListFromListRolesClaimWithOptionalRoleClaim(){
		
		// Given
		String tokenValue = "valueGeneratedByTheOAuthServer";
		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("alg", "RS256");
		headers.put("typ", "JWT");
		headers.put("kid", "RmqNU3K7LxrNRFkHU2qq6Yq12kTCismFL9ScpnCOx0c");
		
		Map<String, Object> claims = new HashMap<String, Object>();
		// user data
		claims.put("sub", "testSub");
		claims.put("given_name", "Richard");
		claims.put("family_name", "HeartLion");	
				
		// groups data
		// test single value claim
		claims.put("roleClaim", "RoleClaimValue");	
		
		JSONObject pathA2 = new JSONObject();
		pathA2.put("roleClaim", "ADMIN");
		JSONObject pathA1 = new JSONObject();
		pathA1.put("pathA2", pathA2);
		claims.put("pathA1", pathA1);
		
		// test multiple values claim
		JSONObject pathB3 = new JSONObject();
		pathB3.put("listRoleClaim", "Role1;Role2;Role3"); 		// test the separator ";"
		JSONObject pathB2 = new JSONObject();
		pathB2.put("pathB3", pathB3);
		JSONObject pathB1 = new JSONObject();
		pathB1.put("pathB2", pathB2);
		claims.put("pathB1", pathB1);
		
		JSONObject pathC1 = new JSONObject();
		pathC1.put("listRoleClaim", "RoleA,RoleB");				// test the separator ",'
		claims.put("pathC1", pathC1);
		
		// test no mandatory claim
		// test if the path "pathD1/roleClaimOptional1" exists, after roleClaimOptional1, it doesn't matter
		// case the value of roleClaimOptional1 is a JSONObject 
		JSONObject othersD2 = new JSONObject();
		othersD2.put("othersD2", "Value not important");
		JSONObject pathD1 = new JSONObject();
		pathD1.put("RoleClaimOptional1", othersD2);				
		claims.put("pathD1", pathD1);
		
		// test if the path exists in "pathE1/pathE2/roleClaimOptional2", after roleClaimOptional2, it doesn't matter
		// case the value of roleClaimOptional1 is a value
		JSONObject pathE2 = new JSONObject();
		pathE2.put("RoleClaimOptional2", "Value not important");
		JSONObject pathE1 = new JSONObject();
		pathE1.put("pathE2", pathE2);				
		claims.put("pathE1", pathE1);
		
		Jwt jwt = new Jwt(tokenValue, Instant.ofEpochMilli(0), Instant.now(), headers, claims);
		
		// Test
		List<GrantedAuthority> listGrantedAuthorityActual = groupsUtils.createAuthorityList(jwt);
		
		// Result
		log.info(listGrantedAuthorityActual.toString());
		
        assertThat(listGrantedAuthorityActual, hasSize(9));
        assertThat("must contains the ROLE_RoleClaimValue", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_RoleClaimValue")));
        assertThat("must contains the ROLE_ADMIN", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertThat("must contains the ROLE_Role1", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_Role1")));
        assertThat("must contains the ROLE_Role2", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_Role2")));
        assertThat("must contains the ROLE_Role3", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_Role3")));
        assertThat("must contains the ROLE_RoleA", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_RoleA")));
        assertThat("must contains the ROLE_RoleB", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_RoleB")));
        assertThat("must contains the ROLE_RoleA", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_RoleClaimOptional1")));
        assertThat("must contains the ROLE_RoleB", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_RoleClaimOptional2")));
          
    }
	
	@Test
    public void createAuthorityListFromListRolesClaimWithoutOptionalRoleClaim(){
		
		// Given
		String tokenValue = "valueGeneratedByTheOAuthServer";
		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("alg", "RS256");
		headers.put("typ", "JWT");
		headers.put("kid", "RmqNU3K7LxrNRFkHU2qq6Yq12kTCismFL9ScpnCOx0c");
		
		Map<String, Object> claims = new HashMap<String, Object>();
		// user data
		claims.put("sub", "testSub");
		claims.put("given_name", "Richard");
		claims.put("family_name", "HeartLion");	
				
		// groups data
		// test single value claim
		claims.put("roleClaim", "RoleClaimValue");	
		
		JSONObject pathA2 = new JSONObject();
		pathA2.put("roleClaim", "ADMIN");
		JSONObject pathA1 = new JSONObject();
		pathA1.put("pathA2", pathA2);
		claims.put("pathA1", pathA1);
		
		// test multiple values claim
		JSONObject pathB3 = new JSONObject();
		pathB3.put("listRoleClaim", "Role1;Role2;Role3"); 		// test the separator ";"
		JSONObject pathB2 = new JSONObject();
		pathB2.put("pathB3", pathB3);
		JSONObject pathB1 = new JSONObject();
		pathB1.put("pathB2", pathB2);
		claims.put("pathB1", pathB1);
		
		JSONObject pathC1 = new JSONObject();
		pathC1.put("listRoleClaim", "RoleA,RoleB");				// test the separator ",'
		claims.put("pathC1", pathC1);
		
		Jwt jwt = new Jwt(tokenValue, Instant.ofEpochMilli(0), Instant.now(), headers, claims);
		
		// Test
		List<GrantedAuthority> listGrantedAuthorityActual = groupsUtils.createAuthorityList(jwt);
		
		// Result
		log.info(listGrantedAuthorityActual.toString());
		
        assertThat(listGrantedAuthorityActual, hasSize(7));
        assertThat("must contains the ROLE_RoleClaimValue", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_RoleClaimValue")));
        assertThat("must contains the ROLE_ADMIN", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertThat("must contains the ROLE_Role1", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_Role1")));
        assertThat("must contains the ROLE_Role2", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_Role2")));
        assertThat("must contains the ROLE_Role3", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_Role3")));
        assertThat("must contains the ROLE_RoleA", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_RoleA")));
        assertThat("must contains the ROLE_RoleB", listGrantedAuthorityActual.contains(new SimpleGrantedAuthority("ROLE_RoleB")));
          
    }
		
}
