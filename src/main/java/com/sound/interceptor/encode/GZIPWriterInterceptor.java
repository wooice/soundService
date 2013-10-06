package com.sound.interceptor.encode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

public class GZIPWriterInterceptor implements WriterInterceptor, ContainerResponseFilter {

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException,
      WebApplicationException {
    if (null != context.getHeaders().get("Content-Encoding")
        && context.getHeaders().get("Content-Encoding").contains("gzip")) {
      final OutputStream outputStream = context.getOutputStream();
      context.setOutputStream(new GZIPOutputStream(outputStream));
    }

    context.proceed();
  }

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    if (requestContext.getHeaderString("Accept-Encoding").contains("gzip")
        && (responseContext.getStatus() == 200 || responseContext.getStatus() == 201)) {
      responseContext.getHeaders().add("Content-Encoding", "gzip");
    }
  }
}
