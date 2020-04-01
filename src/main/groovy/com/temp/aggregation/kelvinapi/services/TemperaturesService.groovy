package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.Temperature
import com.temp.aggregation.kelvinapi.repositories.TemperatureRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class TemperaturesService {

    @Autowired
    TemperatureRepository repository

    Page<Temperature> getTemperaturesFor(String organizationId, Pageable pageable) {
        return repository.findAllByOrganizationId(organizationId, pageable)
    }

    List<Temperature> saveAll(List<Temperature> temperatures) {
        return repository.saveAll(temperatures)
    }
}
