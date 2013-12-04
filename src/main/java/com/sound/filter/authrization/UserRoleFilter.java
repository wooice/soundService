package com.sound.filter.authrization;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRoleFilter implements Filter {

  Logger logger = Logger.getLogger(UserRoleFilter.class);

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Override
  public void init(FilterConfig arg0) throws ServletException {}

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain next)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpSession session = request.getSession(false);

    String userAlias = (null == session) ? null : (String) session.getAttribute("userAlias");
    
    @SuppressWarnings("unchecked")
    List<String> userRoles =
        (null == session) ? null : (List<String>) session.getAttribute("userRoles");

    next.doFilter(new UserRoleRequestWrapper(request, userAlias, userRoles), res);
  }

  @Override
  public void destroy() {

  }
}
