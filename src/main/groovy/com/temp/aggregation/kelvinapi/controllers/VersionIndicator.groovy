package com.temp.aggregation.kelvinapi.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component

@Component
class VersionIndicator extends AbstractHealthIndicator {

  @Autowired(required = false)
  BuildProperties buildProperties

  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {
    builder.withDetail('version', buildProperties?.version ?: 'UNDEFINED')
    builder.up()
  }
}
