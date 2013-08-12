package com.sound.service.storage.itf;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.sound.exception.RemoteStorageException;
import com.sound.model.enums.FileType;

public interface RemoteStorageService {

  public void downloadToFile(String fileName, String fullPath, FileType type)
      throws RemoteStorageException;

  public InputStream downloadToMemory(String fileName, FileType type) throws RemoteStorageException;

  public void upload(String fileName, InputStream content, FileType type)
      throws RemoteStorageException;

  public void delete(String fileName, FileType type) throws RemoteStorageException;

  public PropertiesConfiguration getRemoteStorageConfig();

  public URL generateDownloadUrl(String file, FileType type);

  public URL generateUploadUrl(String file, FileType type);
}
