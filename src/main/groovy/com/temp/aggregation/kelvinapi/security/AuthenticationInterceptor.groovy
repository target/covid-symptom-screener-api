package com.temp.aggregation.kelvinapi.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthenticationInterceptor extends HandlerInterceptorAdapter {
  @Value('${google-auth.enabled}')
  boolean enabled

  @Autowired
  AuthenticationService validationService

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
