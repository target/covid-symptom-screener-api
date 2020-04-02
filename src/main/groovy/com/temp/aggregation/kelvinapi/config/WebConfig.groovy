package com.temp.aggregation.kelvinapi.config

import com.temp.aggregation.kelvinapi.authentication.AuthenticationInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig implements WebMvcConfigurer {
  @Override
  void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new AuthenticationInterceptor())
      .excludePathPatterns('/health', '/info')
  }

}
