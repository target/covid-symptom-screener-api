package com.temp.aggregation.kelvinapi.security

import com.temp.aggregation.kelvinapi.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthenticationService {

  @Autowired
  TokenValidationService tokenValidationService

  @Autowired
  RequestContext requestContext

  boolean isAuthenticated(String token) {
    User user = tokenValidationService.validateAuthToken(token)
    requestContext.userContext = user
    return user != null
  }
}
