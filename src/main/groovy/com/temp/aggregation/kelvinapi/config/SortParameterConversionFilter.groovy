package com.temp.aggregation.kelvinapi.config

import com.google.common.base.CaseFormat
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

@Slf4j
@Component
class SortParameterConversionFilter extends OncePerRequestFilter {

  private static final String SORT_PARAM_KEY = 'sort'

  @Override
  void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    if (shouldApply(request)) {
      chain.doFilter(new CollectionResourceRequestWrapper(request), response)
    } else {
      chain.doFilter(request, response)
    }
  }

  /**
   *
   * @param request
   * @return Request should only be wrapped if it contains sorts
   */
  protected static boolean shouldApply(HttpServletRequest request) {
    return request.getParameterNames().find { it == SORT_PARAM_KEY }
  }

  /**
   * HttpServletRequestWrapper implementation which allows us to wrap and
   * modify the incoming request.
   *
   */
  static class CollectionResourceRequestWrapper extends HttpServletRequestWrapper {

    CollectionResourceRequestWrapper(HttpServletRequest request) {
      super(request)
    }

    @Override
    String[] getParameterValues(String name) {

      if (name.equalsIgnoreCase(SORT_PARAM_KEY)) {

        String[] transformedSorts = this.request.getParameterValues(name).collect {
          String[] parts = it.split(',')

          return parts.collect { String part ->
            if (part.contains('_')) {
              return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, part)
            }
            return part
          }.join(',')
        }
        return transformedSorts
      }

      return this.request.getParameterValues(name)
    }

  }
}
