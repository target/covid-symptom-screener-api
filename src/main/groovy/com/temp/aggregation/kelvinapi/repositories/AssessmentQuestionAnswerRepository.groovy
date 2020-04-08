package com.temp.aggregation.kelvinapi.repositories

import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionAnswerDTO
import com.temp.aggregation.kelvinapi.domain.TemperatureDTO
import org.springframework.data.jpa.repository.JpaRepository

interface AssessmentQuestionAnswerRepository extends JpaRepository<AssessmentQuestionAnswerDTO, String> {
  List<AssessmentQuestionAnswerDTO> findAllByTemperature(TemperatureDTO temperatureDTO)
}
