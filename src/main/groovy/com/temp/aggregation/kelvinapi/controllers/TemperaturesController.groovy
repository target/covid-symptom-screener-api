package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.Temperature
import com.temp.aggregation.kelvinapi.domain.TemperatureListResponse
import com.temp.aggregation.kelvinapi.services.TemperaturesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class TemperaturesController {
    @Autowired
    TemperaturesService service

    @GetMapping('/temperatures')
    @ResponseStatus(HttpStatus.OK)
    TemperatureListResponse getTemperatures(@RequestParam('organization_id') String organizationId,
                                            Pageable pageable) {
        Page<Temperature> temperatures = service.getTemperaturesFor(organizationId, pageable)
        return new TemperatureListResponse(results: temperatures.content)
    }

    @PostMapping('/temperatures')
    @ResponseStatus(HttpStatus.CREATED)
    TemperatureListResponse saveTemperatures(@RequestBody List<Temperature> temperatures) {
        List<Temperature> saved = service.saveAll(temperatures)
        return new TemperatureListResponse(results: saved)
    }
}
