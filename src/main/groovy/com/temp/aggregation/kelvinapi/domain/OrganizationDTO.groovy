package com.temp.aggregation.kelvinapi.domain

import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.*
import java.time.Instant

@Entity
@Table(
    name = 'organizations',
    indexes = [
        @Index(name = 'auth_code_index', columnList = 'authorizationCode', unique = true),
        @Index(name = 'tax_code_index', columnList = 'taxId', unique = true),
        @Index(name = 'org_name_index', columnList = 'orgName', unique = true)
    ]
)
@EntityListeners(AuditingEntityListener)
class OrganizationDTO {
  @Id
  @GeneratedValue(generator = 'system-uuid')
  @GenericGenerator(name = 'system-uuid', strategy = 'uuid')
  String id
  String authorizationCode
  String taxId
  String orgName
  String contactName
  String contactEmail
  String contactJobTitle
  String contactPhone
  ApprovalStatus approvalStatus = ApprovalStatus.APPLIED
  OrganizationSector sector
  @CreatedDate
  Instant created
  @CreatedBy
  String createdBy
  @LastModifiedDate
  Instant lastModified
  @LastModifiedBy
  String lastModifiedBy
}
