package com.sound.service.storage.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.rs.GetPolicy;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;
import com.qiniu.api.rs.URLUtils;
import com.qiniu.api.auth.AuthException;
import com.sound.constant.Constant;
import com.sound.exception.RemoteStorageException;

@Service
@Scope("singleton")
public class RemoteStorageService implements com.sound.service.storage.itf.RemoteStorageService {

  private static final String CONFIG_FILE = "storeConfig.properties";

  private PropertiesConfiguration config;

  private Mac mac;

  public RemoteStorageService() throws RemoteStorageException {
    loadPropertiesConfiguration();

    initClientConfig();
  }

  private void initClientConfig() {
    mac = new Mac(config.getString("ACCESS_KEY"), config.getString("SECRET_KEY"));
  }

  private void loadPropertiesConfiguration() throws RemoteStorageException {
    try {
      config = new PropertiesConfiguration(CONFIG_FILE);

    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    if (!config.containsKey("ACCESS_KEY") || !config.containsKey("SECRET_KEY")
        || !config.containsKey("SOUND_BUCKET") || !config.containsKey("IMAGE_BUCKET")) {
      throw new RemoteStorageException(
          "storage config should have ACCESS_KEY, Secret_KEY, IMAGE_BUCKET and SOUND_BUCKET config");
    }
  }

  public Map<String, String> getSoundUploadInfo(String fileKey) {
    PutSound putSound = new PutSound();
    putSound.key = fileKey;
    putSound.type = "sound";
    putSound.asyncOps = "avthumb/wav/acodec/pcm_s16le";
    putSound.persistentOps = "avthumb/mp3";
    putSound.persistentNotifyUrl = "http://www.baidu.com";

    Map<String, String> uploadInfo = new HashMap<String, String>();
    uploadInfo.put("token", generateUpToken(putSound));

    return uploadInfo;
  }

  public Map<String, String> getImageUploadInfo(String fileKey) {
    PutSound putSound = new PutSound();
    putSound.key = fileKey;
    putSound.type = "image";

    Map<String, String> uploadInfo = new HashMap<String, String>();
    uploadInfo.put("token", generateUpToken(putSound));

    return uploadInfo;
  }

  public String getDownloadURL(String fileName, String type, String format) {
    GetSound getSound = new GetSound();
    getSound.key = fileName;
    getSound.type = type;

    return generateDownUrl(getSound, format);
  }
  
  public void deleteFile(String type, String fileKey) {
    RSClient client = new RSClient(mac);
    if (type.equals("sound")) {
      client.delete(config.getString("SOUND_BUCKET"), fileKey);
    } else {
      if (type.equals("wave"))
      {
        client.delete(config.getString("IMAGE_BUCKET"), fileKey);
      }
      else
      {
        client.delete(config.getString("IMAGE_BUCKET"), fileKey);
      }
    }
  }
  
  @Override
  public void uploadFile(String type, String fileKey, String filePath) {
    PutPolicy putPolicy = new PutPolicy(config.getString("WAVE_BUCKET"));
    try {
      String uptoken = putPolicy.token(mac);
      PutExtra extra = new PutExtra();
      IoApi.putFile(uptoken, fileKey, filePath, extra);
    } catch (AuthException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getSoundInfo(String remoteId) {
    String infoURL = this.getDownloadURL(remoteId, "sound", "avinfo");
    try {
      HttpClient httpClient = new DefaultHttpClient();
      HttpGet httpget = new HttpGet(infoURL);
      HttpResponse httpresponse = httpClient.execute(httpget);
      // 获取返回数据
      HttpEntity entity = httpresponse.getEntity();
      return EntityUtils.toString(entity, "UTF-8");
    } catch (Exception e) {
      return null;
    }
  }

  //http://developer.qiniu.com/docs/v6/api/reference/security/access-token.html
  @Override
  public String generateToken(String urlToGenerate)
  {
	  if (null == urlToGenerate)
	  {
		  return null;
	  }
	  
	  try {
		return mac.sign(urlToGenerate.getBytes());
	} catch (AuthException e) {
		e.printStackTrace();
		return null;
	}
  }
  
  @Override
  public String getBucket(String type)
  {
	  if ("sound".equals(type))
	  {
		  return config.getString("SOUND_BUCKET");
	  }
	  else
	  {
		  if ("image".equals(type))
		  {
			  return config.getString("IMAGE_BUCKET");	 
		  }
		  else
		  {
			  return config.getString("SOUND_BUCKET"); 
		  }
	  }
  }
  
  private String generateDownUrl(GetSound input, String format) {
    try {
      String baseUrl = null;

      if (input.type.equals("sound")) {
        baseUrl =
            URLUtils.makeBaseUrl(config.getString("SOUND_DOMAIN"), input.key)
                + ((null == format) ? "" : "?" + format);
      } else {
        baseUrl =
            URLUtils.makeBaseUrl(config.getString("IMAGE_DOMAIN"), input.key)
                + ((null == format) ? "" : "?" + format);
      }

      return getDownloadToken(baseUrl, input.type);
    } catch (EncoderException e) {
      e.printStackTrace();
    }

    return null;
  }
  
  private String getDownloadToken(String baseUrl, String type) {
    GetPolicy getPolicy = new GetPolicy();
    if (type.equals("sound"))
    {
      getPolicy.expires = Constant.SOUND_CACHE_TIME / 1000;
    }

    try {
      return getPolicy.makeRequest(baseUrl, mac);
    } catch (AuthException e) {
      e.printStackTrace();
    }

    return null;
  }

  private String generateUpToken(PutSound input) {
    PutPolicy putPolicy = null;
    if (input.type.equals("sound")) {
      putPolicy = new PutPolicy(config.getString("SOUND_BUCKET") + ":" + input.key);
    } else {
      putPolicy = new PutPolicy(config.getString("IMAGE_BUCKET") + ":" + input.key);
    }
  
    putPolicy.asyncOps = input.asyncOps;
    putPolicy.persistentOps = input.persistentOps;
    putPolicy.persistentNotifyUrl = input.persistentNotifyUrl;
    putPolicy.callbackBody = input.callbackBody;
    putPolicy.callbackUrl = input.callbackUrl;
    putPolicy.endUser = input.endUser;
    putPolicy.expires = config.getLong("AccessExpires", 300000);
    putPolicy.returnBody = input.returnBody;
    putPolicy.returnUrl = input.returnUrl;
  
    try {
      String uptoken = putPolicy.token(mac);
  
      return uptoken;
    } catch (AuthException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  
    return null;
  }
}
