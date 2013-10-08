package com.sound.jackson.extension;

import java.io.IOException;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class IdSerializer extends JsonSerializer<ObjectId> {

  @Override
  public void serialize(ObjectId value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    jgen.writeString(value.toString());
  }

}
