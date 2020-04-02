package com.temp.aggregation.kelvinapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {

  @Value('#{\'${cors.origins}\'.split(\',\')}')
  List<String> allowedOrigins

  @Bean
  FilterRegistrationBean corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource()
    CorsConfiguration config = new CorsConfiguration()
    config.allowCredentials = true

    allowedOrigins.each {
      config.addAllowedOrigin(it)
    }

    config.addAllowedHeader('*')
    config.addAllowedMethod('*')
    source.registerCorsConfiguration('/**', config)
    FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source))
    bean.order = 0
    return bean
  }
}
