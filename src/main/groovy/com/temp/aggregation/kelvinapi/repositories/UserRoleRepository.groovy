package com.temp.aggregation.kelvinapi.repositories

import com.temp.aggregation.kelvinapi.domain.Role
import com.temp.aggregation.kelvinapi.domain.UserRoleDTO
import org.springframework.data.jpa.repository.JpaRepository

interface UserRoleRepository extends JpaRepository<UserRoleDTO, String> {
  boolean existsByEmailAddressAndRole(String emailAddress, Role role)
}
