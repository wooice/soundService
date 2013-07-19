package com.sound.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.github.jmkgreen.morphia.annotations.Entity;

@Entity(noClassnameStored= true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag extends BaseModel
{

}
