package com.temp.aggregation.kelvinapi.authentication

import com.temp.aggregation.kelvinapi.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserAuthorizationService {

  @Autowired
  TokenValidationService tokenValidationService

  boolean isAuthenticated(String token) {
    User user = tokenValidationService.validateAuthToken(token)
    return user != null
  }
}
