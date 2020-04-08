package com.temp.aggregation.kelvinapi.integration

import spock.util.concurrent.PollingConditions

trait IntegrationPollingTrait {
  @Delegate PollingConditions conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.1)
}
