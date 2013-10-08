package com.sound.jackson.extension;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializationConfig.Feature;

@Provider
public class SoundModelMapperProvider implements ContextResolver<ObjectMapper> {

  final ObjectMapper objectMapper;

  public SoundModelMapperProvider() {
    objectMapper = new ObjectMapper();

    objectMapper.configure(Feature.INDENT_OUTPUT, true);
    objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }

}
