package com.temp.aggregation.kelvinapi.repositories

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.Organization
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizationRepository extends JpaRepository<Organization, String> {

  Organization findByTaxId(String taxId)

  Page<Organization> findAllByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable)

  Page<Organization> findAllByOrgNameContainingIgnoreCase(
      String orgName,
      Pageable pageable)

  Page<Organization> findAllByApprovalStatusAndOrgNameContainingIgnoreCase(
      ApprovalStatus approvalStatus,
      String orgName,
      Pageable pageable)

  Organization findByApprovalStatusAndAuthorizationCode(ApprovalStatus status, String authorizationCode)
}
