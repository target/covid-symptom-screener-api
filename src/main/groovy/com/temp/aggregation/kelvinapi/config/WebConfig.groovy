package com.temp.aggregation.kelvinapi.config

import com.temp.aggregation.kelvinapi.security.AuthenticationInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig implements WebMvcConfigurer {

  @Autowired
  AuthenticationInterceptor authenticationInterceptor

  @Override
  void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authenticationInterceptor)
        .excludePathPatterns('/health', '/info')
        .order(Ordered.HIGHEST_PRECEDENCE)
  }
}
