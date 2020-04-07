package com.temp.aggregation.kelvinapi.repositories

import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionDTO
import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestionDTO, String> {
  boolean existsByDisplayValue(String displayValue)
  List<AssessmentQuestionDTO> findAllByStatus(AssessmentQuestionStatus status, Sort sort)
  List<AssessmentQuestionDTO> findAllByStatusIn(List<AssessmentQuestionStatus> statuses, Sort sort)
}
