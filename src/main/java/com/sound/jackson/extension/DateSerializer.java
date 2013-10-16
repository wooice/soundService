package com.sound.jackson.extension;

import java.io.IOException;
import java.util.Date;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class DateSerializer extends JsonSerializer<Date> {

  @Override
  public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    Date now = new Date();
    long diff = (now.getTime() - value.getTime())/ (60 * 1000);
    
    if (diff < 3)
    {
      jgen.writeString("刚刚");
      return;
    }
    
    if (diff < 60)
    {
      jgen.writeString(diff+"分钟前");
      return;
    }
    
    for (int i=2; i<= 24; i++)
    {
      if (diff < i * 60)
      {
        jgen.writeString((i-1)+"小时前");
        return;
      }
    }
    
    diff = diff/60;
    
    for (int i=2; i <= 31; i++)
    {
      if (diff < i * 24)
      {
        jgen.writeString((i-1)+"天前");
        return;
      }
    }
    
    diff = diff/24;
    int year = (int) Math.floor(diff/365);
    int month = (int) Math.floor((diff%365)/30);
    
    jgen.writeString(((year>0)? (year+"年"):"") + (month+"个月前"));
  }

}
