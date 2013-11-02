package com.sound.model;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.sound.jackson.extension.IdSerializer;

@Entity(noClassnameStored = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoundInfringe {
  @Id
  private ObjectId id;
  
  private String links;
  private String ownerRight;
  private String rightIssue;
  private String iswc;
  private String status;
  @Embedded
  private Informer informer;
  @Embedded
  private Confirm confirm;
  
  @JsonSerialize(using = IdSerializer.class)
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getLinks() {
    return links;
  }

  public void setLinks(String links) {
    this.links = links;
  }

  public String getOwnerRight() {
    return ownerRight;
  }

  public void setOwnerRight(String ownerRight) {
    this.ownerRight = ownerRight;
  }

  public String getRightIssue() {
    return rightIssue;
  }

  public void setRightIssue(String rightIssue) {
    this.rightIssue = rightIssue;
  }

  public String getIswc() {
    return iswc;
  }

  public void setIswc(String iswc) {
    this.iswc = iswc;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Informer getInformer() {
    return informer;
  }

  public void setInformer(Informer informer) {
    this.informer = informer;
  }

  public Confirm getConfirm() {
    return confirm;
  }

  public void setConfirm(Confirm confirm) {
    this.confirm = confirm;
  }

  public static class Informer{
    private String xing;
    private String ming;
    private String company;
    private String rightsHolder;
    private String street;
    private String city;
    private String postalCode;
    private String country;
    private String email;
    private String mobile;
    private String phone;
    @Reference
    private User user;
    
    public User getUser() {
      return user;
    }
    public void setUser(User user) {
      this.user = user;
    }
    public String getXing() {
      return xing;
    }
    public void setXing(String xing) {
      this.xing = xing;
    }
    public String getMing() {
      return ming;
    }
    public void setMing(String ming) {
      this.ming = ming;
    }
    public String getCompany() {
      return company;
    }
    public void setCompany(String company) {
      this.company = company;
    }
    public String getRightsHolder() {
      return rightsHolder;
    }
    public void setRightsHolder(String rightsHolder) {
      this.rightsHolder = rightsHolder;
    }
    public String getStreet() {
      return street;
    }
    public void setStreet(String street) {
      this.street = street;
    }
    public String getCity() {
      return city;
    }
    public void setCity(String city) {
      this.city = city;
    }
    public String getPostalCode() {
      return postalCode;
    }
    public void setPostalCode(String postalCode) {
      this.postalCode = postalCode;
    }
    public String getCountry() {
      return country;
    }
    public void setCountry(String country) {
      this.country = country;
    }
    public String getEmail() {
      return email;
    }
    public void setEmail(String email) {
      this.email = email;
    }
    public String getMobile() {
      return mobile;
    }
    public void setMobile(String mobile) {
      this.mobile = mobile;
    }
    public String getPhone() {
      return phone;
    }
    public void setPhone(String phone) {
      this.phone = phone;
    }
    
  }
  
  public static class Confirm{
    private boolean notAuthorized;
    private boolean guarantee;
    private boolean responsibility;
    private String sign;
    public boolean isNotAuthorized() {
      return notAuthorized;
    }
    public void setNotAuthorized(boolean notAuthorized) {
      this.notAuthorized = notAuthorized;
    }
    public boolean isGuarantee() {
      return guarantee;
    }
    public void setGuarantee(boolean guarantee) {
      this.guarantee = guarantee;
    }
    public boolean isResponsibility() {
      return responsibility;
    }
    public void setResponsibility(boolean responsibility) {
      this.responsibility = responsibility;
    }
    public String getSign() {
      return sign;
    }
    public void setSign(String sign) {
      this.sign = sign;
    }
  }
}
