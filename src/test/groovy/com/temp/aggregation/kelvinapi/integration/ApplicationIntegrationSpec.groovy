package com.temp.aggregation.kelvinapi.integration

class ApplicationIntegrationSpec extends BaseIntegrationSpec {
    void 'got application context'() {
        expect:
        serverPort == 8080
        env.activeProfiles.toList() == ['integration']
        applicationContext != null
    }
}
