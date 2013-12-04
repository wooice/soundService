package com.sound.filter.authrization;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@Resource
public class UserRoleRequestWrapper extends HttpServletRequestWrapper {

  String user;
  List<String> roles = null;
  HttpServletRequest request;

  public UserRoleRequestWrapper(HttpServletRequest request, String user, List<String> roles) {
    super(request);

    this.request = request;
    this.user = user;
    this.roles = roles;
  }

  @Override
  public boolean isUserInRole(String role) {
    if (roles == null) {
      // If no role on current user, treat him as a guest.
      if ("guest".equals(role)) {
        return true;
      }
      return this.request.isUserInRole(role);
    }
    return roles.contains(role);
  }

  @Override
  public Principal getUserPrincipal() {
    if (this.user == null) {
      return this.request.getUserPrincipal();
    }

    return new Principal() {
      @Override
      public String getName() {
        return user;
      }
    };
  }
}
