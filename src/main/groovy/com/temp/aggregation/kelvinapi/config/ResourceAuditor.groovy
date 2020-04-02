package com.temp.aggregation.kelvinapi.config

import com.temp.aggregation.kelvinapi.security.RequestContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.AuditorAware

class ResourceAuditor implements AuditorAware<String> {

  public static final String DEFAULT_AUDITOR = 'SYSTEM'

  @Autowired
  RequestContext requestContext

  @Override
  Optional<String> getCurrentAuditor() {
    return Optional.of(requestContext.userContext?.email ?: DEFAULT_AUDITOR)
  }
}
