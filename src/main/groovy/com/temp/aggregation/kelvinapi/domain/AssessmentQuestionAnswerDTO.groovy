package com.temp.aggregation.kelvinapi.domain

import org.hibernate.annotations.GenericGenerator

import javax.persistence.*

@Entity
@Table(
    name = 'assessment_question_answers'
)
class AssessmentQuestionAnswerDTO {
  @Id
  @GeneratedValue(generator = 'system-uuid')
  @GenericGenerator(name = 'system-uuid', strategy = 'uuid')
  String id

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = 'question_id', referencedColumnName = 'id')
  AssessmentQuestionDTO question

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = 'temperature_id', referencedColumnName = 'id')
  TemperatureDTO temperature

  Boolean answer
}
