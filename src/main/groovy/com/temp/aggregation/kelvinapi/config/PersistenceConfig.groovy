package com.temp.aggregation.kelvinapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing(auditorAwareRef = 'auditorAware')
class PersistenceConfig {

  @Bean
  AuditorAware<String> auditorAware() {
    return new ResourceAuditor()
  }
}
