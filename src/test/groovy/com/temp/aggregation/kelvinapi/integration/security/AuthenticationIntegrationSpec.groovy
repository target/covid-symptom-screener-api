package com.temp.aggregation.kelvinapi.integration.security

import com.temp.aggregation.kelvinapi.security.AuthenticationService
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

class AuthenticationIntegrationSpec extends BaseIntegrationSpec {
  @Autowired
  AuthenticationService userAuthorizationService

  void 'Validate auth token returns User from spring bean override'() {
    when:
    boolean isAuthenticated = userAuthorizationService.isAuthenticated('test-token')

    then:
    isAuthenticated
  }
}
