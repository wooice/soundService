package com.sound.service.storage.impl;

public class PutSound {
  public String type;

  public String key;

  /** 可选 */
  public String callbackUrl;
  /** 可选 */
  public String callbackBody;
  
  public String persistentNotifyUrl;
  /** 可选 */
  public String returnUrl;
  /** 可选 */
  public String returnBody;
  /** 可选 */
  public String asyncOps;
  
  public String persistentOps;
  /** 可选 */
  public String endUser;
  /** 可选 */
  public long expires;

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  public String getCallbackBody() {
    return callbackBody;
  }

  public void setCallbackBody(String callbackBody) {
    this.callbackBody = callbackBody;
  }

  public String getPersistentNotifyUrl() {
	return persistentNotifyUrl;
  }

  public void setPersistentNotifyUrl(String persistentNotifyUrl) {
	this.persistentNotifyUrl = persistentNotifyUrl;
  }

public String getReturnUrl() {
    return returnUrl;
  }

  public void setReturnUrl(String returnUrl) {
    this.returnUrl = returnUrl;
  }

  public String getReturnBody() {
    return returnBody;
  }

  public void setReturnBody(String returnBody) {
    this.returnBody = returnBody;
  }

  public String getAsyncOps() {
    return asyncOps;
  }

  public void setAsyncOps(String asyncOps) {
    this.asyncOps = asyncOps;
  }

	public String getPersistentOps() {
		return persistentOps;
	}

	public void setPersistentOps(String persistentOps) {
		this.persistentOps = persistentOps;
	}

public String getEndUser() {
    return endUser;
  }

  public void setEndUser(String endUser) {
    this.endUser = endUser;
  }

  public long getExpires() {
    return expires;
  }

  public void setExpires(long expires) {
    this.expires = expires;
  }

}
