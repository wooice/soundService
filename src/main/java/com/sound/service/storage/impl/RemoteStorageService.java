package com.sound.service.storage.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.aliyun.openservices.ClientConfiguration;
import com.aliyun.openservices.HttpMethod;
import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.openservices.oss.model.GetObjectRequest;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import com.sound.exception.RemoteStorageException;
import com.sound.model.enums.FileType;

@Service
@Scope("singleton")
public class RemoteStorageService implements com.sound.service.storage.itf.RemoteStorageService {

  private static final String OSS_CONFIG_FILE = "ossConfig.properties";

  private static final int DEFAULT_CONEECTIONTIMEOUT = 10000;

  private static final int DEFAULT_MAXCONNECTION = 1000;
  private static final int DEFAULT_MAXERRORRETRY = 3;
  private static final int DEFAULT_SOCKETIMEOUT = 2000;
  private static final String DEFAULT_USERAGENT = "dxd-oss";
  private static final String ENDPOINT = "http://oss.aliyuncs.com";
  private static final long DEFAULT_EXPIRES = 300000;

  /** The oss client instance */
  private OSSClient client;

  /** The client configuration. */
  private ClientConfiguration clientConfig;

  private PropertiesConfiguration config;

  private long expires;

  public RemoteStorageService() throws RemoteStorageException {
    // Load the properties file.
    loadPropertiesConfiguration();

    initClientConfig();

    client =
        new OSSClient(ENDPOINT, config.getString("ACCESS_ID"), config.getString("ACCESS_KEY"),
            clientConfig);
    expires = config.getLong("AccessExpires", DEFAULT_EXPIRES);

    try {
      client.listBuckets();
    } catch (Exception e) {
      client = null;
      throw new RemoteStorageException("Cannot access oss", e);
    }

  }

  private void initClientConfig() {
    // Initialized the configuration.
    clientConfig = new ClientConfiguration();
    clientConfig
        .setConnectionTimeout(config.getInt("ConnectionTimeout", DEFAULT_CONEECTIONTIMEOUT));
    clientConfig.setMaxConnections(config.getInt("MaxConnections", DEFAULT_MAXCONNECTION));
    clientConfig.setMaxErrorRetry(config.getInt("MaxErrorRetry", DEFAULT_MAXERRORRETRY));
    clientConfig.setSocketTimeout(config.getInt("SocketTimeout", DEFAULT_SOCKETIMEOUT));
    clientConfig.setUserAgent(config.getString("UserAgent", DEFAULT_USERAGENT));
  }

  private void loadPropertiesConfiguration() throws RemoteStorageException {
    try {
      config = new PropertiesConfiguration(OSS_CONFIG_FILE);

    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    if (!config.containsKey("ACCESS_ID") || !config.containsKey("ACCESS_KEY")
        || !config.containsKey("SoundBucket") || !config.containsKey("ImageBucket")) {
      throw new RemoteStorageException(
          "OSS config should have ACCESS_ID, ACCESS_KEY and Bucket config");
    }

  }

  @Override
  public void downloadToFile(String fileName, String fullPath, FileType type)
      throws RemoteStorageException {
    if (fileName == null || fileName.trim().equals("")) {
      throw new RemoteStorageException("Cannot download file because input filename is invalid");
    }

    GetObjectRequest getObjectRequest = generateGetObjectRequest(fileName, type);
    try {
      client.getObject(getObjectRequest, new File(fullPath));
    } catch (Exception e) {
      throw new RemoteStorageException("Cannot download file : " + fileName, e);
    }
  }

  @Override
  public InputStream downloadToMemory(String fileName, FileType type) throws RemoteStorageException {
    if (fileName == null || fileName.trim().equals("")) {
      throw new RemoteStorageException("Cannot download file because input filename is invalid");
    }

    GetObjectRequest getObjectRequest = generateGetObjectRequest(fileName, type);

    try {
      return client.getObject(getObjectRequest).getObjectContent();
    } catch (Exception e) {
      throw new RemoteStorageException("Cannot download file : " + fileName, e);
    }

  }

  @Override
  public void upload(String fileName, InputStream content, FileType type)
      throws RemoteStorageException {
    if (content == null) {
      throw new RemoteStorageException("Nothing to upload.");
    }

    try {
      ObjectMetadata meta = generateObjectMetadata(content);
      client.putObject(config.getString(type.getBucketKey()), fileName, content, meta);
    } catch (Exception e) {
      throw new RemoteStorageException("Cannot upload file : " + fileName, e);
    }
  }

  @Override
  public void delete(String fileName, FileType type) throws RemoteStorageException {
    if (fileName == null || fileName.trim().equals("")) {
      throw new RemoteStorageException("Cannot delete file because input filename is invalid");
    }

    try {
      client.deleteObject(config.getString(type.getBucketKey()), fileName);
    } catch (Exception e) {
      throw new RemoteStorageException("Cannot delete file : " + fileName, e);
    }

  }

  @Override
  public PropertiesConfiguration getRemoteStorageConfig() {
    return config;
  }

  @Override
  public URL generateDownloadUrl(String file, FileType type) {
    return client.generatePresignedUrl(config.getString(type.getBucketKey()), file, new Date(
        new Date().getTime() + expires));
  }

  @Override
  public URL generateUploadUrl(String file, FileType type) {
    GeneratePresignedUrlRequest generatePresignedUrlRequest =
        new GeneratePresignedUrlRequest(config.getString(type.getBucketKey()), file);
    generatePresignedUrlRequest.setMethod(HttpMethod.PUT);
    generatePresignedUrlRequest.setExpiration(new Date(new Date().getTime() + expires));
    return client.generatePresignedUrl(generatePresignedUrlRequest);
  }

  private GetObjectRequest generateGetObjectRequest(String fileName, FileType type) {
    GetObjectRequest getObjectRequest =
        new GetObjectRequest(config.getString(type.getBucketKey()), fileName);

    return getObjectRequest;
  }

  private ObjectMetadata generateObjectMetadata(InputStream content) throws IOException {
    ObjectMetadata meta = new ObjectMetadata();

    meta.setContentLength(content.available());
    Date expire = new Date(new Date().getTime() + this.expires);
    meta.setExpirationTime(expire);

    return meta;
  }
}
