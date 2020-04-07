package com.temp.aggregation.kelvinapi.security

import com.temp.aggregation.kelvinapi.domain.Role
import com.temp.aggregation.kelvinapi.domain.UserRoleDTO
import com.temp.aggregation.kelvinapi.domain.UserRole
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.UserRoleRepository
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.*
import org.springframework.stereotype.Service

import static com.temp.aggregation.kelvinapi.domain.Role.ADMIN
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.exact

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
    UserRoleDTO found = userRoleRepository.findById(requestContext.userContext?.email).orElse(
        new UserRoleDTO(emailAddress: requestContext.userContext?.email, role: Role.USER)
    )
    UserRole dto = new UserRole()
    InvokerHelper.setProperties(dto, found.properties)
    return dto
  }

  Page<UserRole> findBy(Role role, String emailAddress, Pageable pageable) {
    ExampleMatcher matcher = ExampleMatcher
        .matchingAll()
        .withMatcher('emailAddress', exact().ignoreCase())
    UserRoleDTO example = new UserRoleDTO(
        emailAddress: emailAddress,
        role: role
    )
    Page<UserRoleDTO> found = userRoleRepository.findAll(Example.of(example, matcher), pageable)
    List<UserRole> dtos = found.content.collect { userRole ->
      UserRole dto = new UserRole()
      InvokerHelper.setProperties(dto, userRole.properties)
      return dto
    }
    return new PageImpl<>(dtos, found.pageable, found.totalElements)
  }

  void deleteUserRole(String emailAddress) {
    userRoleRepository.deleteById(emailAddress)
  }

  UserRole save(UserRole userRoleDTO) {
    UserRoleDTO userRole = new UserRoleDTO()
    InvokerHelper.setProperties(userRole, userRoleDTO.properties)
    UserRoleDTO saved = userRoleRepository.save(userRole)
    UserRole dto = new UserRole()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
  }
}
