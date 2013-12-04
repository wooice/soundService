package com.sound.filter.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.model.AnnotatedMethod;

public class AuthenticationFeature implements DynamicFeature {

  Logger logger = Logger.getLogger(AuthenticationFeature.class);

  @Override
  public void configure(final ResourceInfo resourceInfo, final FeatureContext configuration) {
    if (null != resourceInfo.getResourceClass().getAnnotation(ResourceAllowed.class))
    {
      return;
    }
    
    AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
    if (am.isAnnotationPresent(ResourceAllowed.class)) {
      return;
    }
    
    configuration.register(new ResourceAllowedRequestFilter());
  }

  @Priority(Priorities.AUTHENTICATION)
  public static class ResourceAllowedRequestFilter implements ContainerRequestFilter {
    
    public ResourceAllowedRequestFilter() {}

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Principal principal = requestContext.getSecurityContext().getUserPrincipal();
        String userAlias = (null==principal)? null: principal.getName();
  
        if (null == userAlias) {
          throw new NotAuthorizedException(Response.status(Status.UNAUTHORIZED).build());
        }
      }
  }
}
