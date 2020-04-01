package com.temp.aggregation.kelvinapi.repositories

import com.temp.aggregation.kelvinapi.domain.Temperature
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface TemperatureRepository extends JpaRepository<Temperature, String> {
    Page<Temperature> findAllByOrganizationId(String organizationId, Pageable pageable)
}
