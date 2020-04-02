package com.temp.aggregation.kelvinapi.authentication

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthenticationInterceptor extends HandlerInterceptorAdapter {
  @Value('${google-auth.enabled}')
  boolean enabled

  @Autowired
  UserAuthorizationService validationService

  @Override
  boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String token = request.getHeader('Authorization')
    if (!enabled || validationService.isAuthenticated(token)) {
      return true
    }
    response.status = HttpStatus.UNAUTHORIZED.value()
    return false
  }
}
