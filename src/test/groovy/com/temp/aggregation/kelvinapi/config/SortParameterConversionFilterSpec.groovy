package com.temp.aggregation.kelvinapi.config

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import spock.lang.Unroll

class SortParameterConversionFilterSpec extends Specification {

  SortParameterConversionFilter.CollectionResourceRequestWrapper wrapper

  @Unroll
  void 'request sort parameter is transformed to camel case'() {
    given:
    MockHttpServletRequest httpServletRequest = new MockHttpServletRequest()
    httpServletRequest.addParameter('other', 'should_stay')
    httpServletRequest.addParameter('sort', 'field,asc')
    httpServletRequest.addParameter('sort', 'fieldName,asc')
    httpServletRequest.addParameter('sort', 'field_name,asc')

    wrapper = new SortParameterConversionFilter.CollectionResourceRequestWrapper(httpServletRequest)

    when:
    String[] otherValues = wrapper.getParameterValues('other')
    String[] sortValues = wrapper.getParameterValues('sort')

    then:
    otherValues as List == ['should_stay']
    sortValues as List == ['field,asc', 'fieldName,asc', 'fieldName,asc']

  }
}
