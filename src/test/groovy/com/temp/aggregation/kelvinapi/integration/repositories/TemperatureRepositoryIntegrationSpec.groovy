package com.temp.aggregation.kelvinapi.integration.repositories

import com.temp.aggregation.kelvinapi.domain.TemperatureDTO
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.repositories.TemperatureRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

import static org.springframework.data.domain.Sort.Direction.ASC

class TemperatureRepositoryIntegrationSpec extends BaseIntegrationSpec {

    @Autowired
    TemperatureRepository repository

    void cleanup() {
        repository.deleteAll()
    }

    void 'can save a list of temperatures, and retrieve them by org id'() {
        setup:
        String organizationId = 'testOrg'
        List<TemperatureDTO> temperatureDTOs = [
                new TemperatureDTO(
                        organizationId: organizationId,
                        temperature: 98.6,
                        userId: 'test-user-a',
                        latitude: 44.934940,
                        longitude: -93.158660
                ),
                new TemperatureDTO(
                        organizationId: organizationId,
                        temperature: 100.5,
                        userId: 'test-user-b',
                        latitude: 44.934941,
                        longitude: -93.158661
                )
        ]

        when:
        List<TemperatureDTO> results = repository.saveAll(temperatureDTOs)

        then:
        results.size() == 2
        !results*.id.contains(null)
        !results*.timestamp.contains(null)

        when:
        Page<TemperatureDTO> retrieved = repository.findAllByOrganizationId(
                organizationId,
                new PageRequest(0, 10, new Sort(ASC, ['id']))
        )

        then:
        retrieved.totalPages == 1
        retrieved.content.size() == 2
        retrieved.content*.temperature.containsAll([98.6f, 100.5f])
        retrieved.content*.userId.containsAll(['test-user-a', 'test-user-b'])
        retrieved.content*.organizationId.unique() == [organizationId]
        retrieved.content*.latitude.containsAll([44.934940f, 44.934941f])
        retrieved.content*.longitude.containsAll([-93.158660f, -93.158661f])
    }
}
