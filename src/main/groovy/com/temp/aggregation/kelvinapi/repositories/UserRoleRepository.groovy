package com.temp.aggregation.kelvinapi.repositories

import com.temp.aggregation.kelvinapi.domain.Role
import com.temp.aggregation.kelvinapi.domain.UserRole
import org.springframework.data.jpa.repository.JpaRepository

interface UserRoleRepository extends JpaRepository<UserRole, String> {
  boolean existsByEmailAddressAndRole(String emailAddress, Role role)
}
