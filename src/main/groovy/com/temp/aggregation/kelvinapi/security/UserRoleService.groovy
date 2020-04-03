package com.temp.aggregation.kelvinapi.security

import com.temp.aggregation.kelvinapi.domain.Role
import com.temp.aggregation.kelvinapi.domain.UserRole
import com.temp.aggregation.kelvinapi.domain.UserRoleUpdate
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.UserRoleRepository
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.*

@Service
class UserRoleService {
  @Autowired
  UserRoleRepository userRoleRepository

  @Autowired
  RequestContext requestContext

  boolean userHasRole(String emailAddress, Role role) {
    return userRoleRepository.existsByEmailAddressAndRole(emailAddress, role)
  }

  boolean currentUserHasRole(Role role) {
    return userRoleRepository.existsByEmailAddressAndRole(requestContext.userContext?.email, role)
  }

  void requireAdmin() {
    if (!currentUserHasRole(ADMIN)) {
      throw new ServiceException(ServiceError.UNAUTHORIZED)
    }
  }

  UserRole getCurrentUserRole() {
    return userRoleRepository.findById(requestContext.userContext?.email).orElse(
      new UserRole(emailAddress:  requestContext.userContext?.email, role: Role.USER)
    )
  }

  Page<UserRole> findBy(Role role, String emailAddress, Pageable pageable) {
    ExampleMatcher matcher = ExampleMatcher
      .matchingAll()
      .withMatcher('emailAddress', exact().ignoreCase())
    UserRole example = new UserRole(
        emailAddress: emailAddress,
        role: role
    )
    return userRoleRepository.findAll(Example.of(example, matcher), pageable)
  }

  void deleteUserRole(String emailAddress) {
    userRoleRepository.deleteById(emailAddress)
  }

  UserRole save(UserRoleUpdate userRoleUpdate) {
    UserRole userRole = new UserRole()
    InvokerHelper.setProperties(userRole, userRoleUpdate.properties)
    return userRoleRepository.save(userRole)
  }
}
