package com.temp.aggregation.kelvinapi.domain

import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull
import java.time.Instant

@Entity
@Table(name = 'organizations')
class Organization {
  @Id
  @GeneratedValue(generator='system-uuid')
  @GenericGenerator(name='system-uuid', strategy = 'uuid')
  String id
  String authorizationCode

  @CreatedDate
  Instant created
  @CreatedBy
  String createdBy
  @LastModifiedDate
  Instant lastModified
  @LastModifiedBy
  String lastModifiedBy

  // TODO for now copying these from OrganizationUpdate until I can figure out how to make it work with jpa
  @NotNull
  String taxId
  @NotNull
  String orgName
  String contactName
  String contactEmail
  String contactJobTitle
  String contactPhone
  ApprovalStatus approvalStatus = ApprovalStatus.APPLIED
}
