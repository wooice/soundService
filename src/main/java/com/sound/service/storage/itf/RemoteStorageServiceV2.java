package com.sound.service.storage.itf;

import java.util.Map;

public interface RemoteStorageServiceV2 {

  public Map<String, String> getSoundUploadInfo(String fileKey);

  public Map<String, String> getImageUploadInfo(String fileKey);

  public String getDownloadURL(String fileName, String type, String format);
}
