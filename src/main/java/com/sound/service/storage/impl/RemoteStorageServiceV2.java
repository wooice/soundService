package com.sound.service.storage.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.json.JSONException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.rs.GetPolicy;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;
import com.qiniu.api.rs.URLUtils;
import com.sound.exception.RemoteStorageException;

@Service
@Scope("singleton")
public class RemoteStorageServiceV2 implements com.sound.service.storage.itf.RemoteStorageServiceV2 {

  private static final String CONFIG_FILE = "storeConfig.properties";

  private PropertiesConfiguration config;

  private Mac mac;

  public RemoteStorageServiceV2() throws RemoteStorageException {
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
    putSound.asyncOps = "avthumb/wav/acodec/pcm_u8;avthumb/mp3";

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
    String baseURL = generateDownToken(getSound, format);

    return baseURL;
  }

  private String generateUpToken(PutSound input) {
    PutPolicy putPolicy = null;
    if (input.type.equals("sound")) {
      putPolicy = new PutPolicy(config.getString("SOUND_BUCKET") + ":" + input.key);
    } else {
      putPolicy = new PutPolicy(config.getString("IMAGE_BUCKET") + ":" + input.key);
    }

    putPolicy.asyncOps = input.asyncOps;
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

  private String generateDownToken(GetSound input, String format) {
    GetPolicy getPolicy = new GetPolicy();
    getPolicy.expires = config.getInt("AccessExpires", 300000);

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
      String uptoken = getPolicy.makeRequest(baseUrl, mac);

      return uptoken;
    } catch (AuthException e) {
      e.printStackTrace();
    } catch (EncoderException e) {
      e.printStackTrace();
    }

    return null;
  }

  public void deleteFile(String type, String fileKey) {
    RSClient client = new RSClient(mac);
    if (type.equals("sound")) {
      client.delete(config.getString("SOUND_BUCKET"), fileKey);
    } else {
      client.delete(config.getString("IMAGE_BUCKET"), fileKey);
    }
  }
}
