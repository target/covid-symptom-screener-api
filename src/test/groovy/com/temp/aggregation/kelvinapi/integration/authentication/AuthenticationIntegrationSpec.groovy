package com.temp.aggregation.kelvinapi.integration.authentication

import com.temp.aggregation.kelvinapi.authentication.UserAuthorizationService
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

class AuthenticationIntegrationSpec extends BaseIntegrationSpec {
  @Autowired
  UserAuthorizationService userAuthorizationService

  void 'Validate auth token returns User from spring bean override'() {
    when:
    boolean isAuthenticated = userAuthorizationService.isAuthenticated('test-token')

    then:
    isAuthenticated
  }
}
