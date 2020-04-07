package com.temp.aggregation.kelvinapi.repositories

import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionAnswer
import com.temp.aggregation.kelvinapi.domain.Temperature
import org.springframework.data.jpa.repository.JpaRepository

interface AssessmentQuestionAnswerRepository extends JpaRepository<AssessmentQuestionAnswer, String> {
  List<AssessmentQuestionAnswer> findAllByTemperature(Temperature temperature)
}
