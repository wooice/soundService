package com.sound.filter.auth;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@Resource
public class UserRoleRequestWrapper extends HttpServletRequestWrapper {

  String user ;
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
      if (role == "guest")
      {
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

    // make an anonymous implementation to just return our user
    return new Principal() {
      @Override
      public String getName() {
        return user;
      }
    };
  }
}
