package com.temp.aggregation.kelvinapi.domain

import groovy.transform.EqualsAndHashCode
import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.*
import java.time.Instant

import static com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus.ENABLED

@Entity
@Table(
    name = 'assessment_questions'
)
@EntityListeners(AuditingEntityListener)
@EqualsAndHashCode(excludes = ['answers'])
class AssessmentQuestionDTO {
  @Id
  @GeneratedValue(generator = 'system-uuid')
  @GenericGenerator(name = 'system-uuid', strategy = 'uuid')
  String id
  String displayValue
  AssessmentQuestionStatus status = ENABLED
  int sortPriority
  @OneToMany(mappedBy = 'question', fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  Set<AssessmentQuestionAnswerDTO> answers
  @CreatedDate
  Instant created
  @CreatedBy
  String createdBy
  @LastModifiedDate
  Instant lastModified
  @LastModifiedBy
  String lastModifiedBy
}
