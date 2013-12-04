package com.sound.service.storage.itf;

import java.util.Map;

public interface RemoteStorageService {

  public Map<String, String> getSoundUploadInfo(String fileKey);

  public Map<String, String> getImageUploadInfo(String fileKey);

  public String getDownloadURL(String fileName, String type, String format);
  
  public void deleteFile(String type, String fileKey);
  
  public void uploadFile(String type, String fileKey, String filePath);

  public String getSoundInfo(String remoteId);
}
