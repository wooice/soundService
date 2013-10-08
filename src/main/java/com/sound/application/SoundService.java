package com.sound.application;

import javax.json.stream.JsonGenerator;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.sound.interceptor.encode.GZIPWriterInterceptor;
import com.sound.jackson.extension.SoundModelMapperProvider;

@ApplicationPath("/")
public class SoundService extends ResourceConfig {

  public SoundService() {
    packages("com.sound.service.endpoint");

    register(SseFeature.class);
    register(GZIPWriterInterceptor.class);
    register(RolesAllowedDynamicFeature.class);
    register(SoundModelMapperProvider.class);
    register(JacksonFeature.class);
    property(JsonGenerator.PRETTY_PRINTING, true);
    property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
    property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);
  }

}
