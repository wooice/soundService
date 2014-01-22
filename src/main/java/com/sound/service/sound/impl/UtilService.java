package com.sound.service.sound.impl;

import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.util.words.Words;

@Service
@Scope("singleton")
public class UtilService implements com.sound.service.sound.itf.UtilService {

  private static final String CONFIG_FILE = "wordsfilter.properties";

  private PropertiesConfiguration config;
  private Words words;

  public UtilService() {
    try {
      config = new PropertiesConfiguration();
      config.setEncoding("UTF-8");
      config.load(CONFIG_FILE);
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    words = new Words();
    @SuppressWarnings("unchecked")
    Iterator<String> iter = config.getKeys();
    while(iter.hasNext())
    {
      words.addWord(iter.next());
    }
  }
  
  @Override
  public boolean contianInvalidWords(String content) {
    return words.contains(content);
  }
}
