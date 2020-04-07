package com.temp.aggregation.kelvinapi.domain

import groovy.transform.EqualsAndHashCode
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.*
import java.time.Instant

@Entity
@EntityListeners(AuditingEntityListener)
@Table(
    name = 'user_roles',
    indexes = [
        @Index(name = 'user_email_index', columnList = 'emailAddress', unique = true)
    ]
)
@EqualsAndHashCode
class UserRole {
  @Id
  String emailAddress
  Role role
  @CreatedDate
  Instant created
  @CreatedBy
  String createdBy
  @LastModifiedDate
  Instant lastModified
  @LastModifiedBy
  String lastModifiedBy
}
