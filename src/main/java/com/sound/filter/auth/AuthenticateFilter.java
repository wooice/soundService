package com.sound.filter.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

@PreMatching
public class AuthenticateFilter implements Filter{

  Logger logger = Logger.getLogger(UserRoleFilter.class);

  @Context
  HttpServletRequest req;

  @Override
  public void init(FilterConfig arg0) throws ServletException {}

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain next)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    String path = request.getPathInfo();
    
    if (!path.startsWith("/auth") && !path.startsWith("/guest"))
    {
      HttpSession session = request.getSession(false);
      String userAlias = (null == session) ? null : (String) session.getAttribute("userAlias");
      
      if (null == userAlias)
      {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return ;
      }
    }
    
    next.doFilter(request, response); 
  }

  @Override
  public void destroy() {}
  
}
