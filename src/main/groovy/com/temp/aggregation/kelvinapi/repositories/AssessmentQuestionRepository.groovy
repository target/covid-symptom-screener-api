package com.temp.aggregation.kelvinapi.repositories

import com.temp.aggregation.kelvinapi.domain.AssessmentQuestion
import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, String> {
  boolean existsByDisplayValue(String displayValue)
  List<AssessmentQuestion> findAllByStatus(AssessmentQuestionStatus status, Sort sort)
  List<AssessmentQuestion> findAllByStatusIn(List<AssessmentQuestionStatus> statuses, Sort sort)
}
