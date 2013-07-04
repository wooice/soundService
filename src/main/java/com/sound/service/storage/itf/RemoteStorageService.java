package com.sound.service.storage.itf;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.sound.exception.RemoteStorageException;

public interface RemoteStorageService {
	
	public List<String> listOwnedFiles(String ownerId) throws RemoteStorageException;
	
	public void downloadToFile(String fileName, String fullPath) throws RemoteStorageException;
	
	public InputStream downloadToMemory(String fileName) throws RemoteStorageException;
	
	public void upload(File file) throws RemoteStorageException;
	
	public void delete(String fileName) throws RemoteStorageException;
	
	public PropertiesConfiguration getRemoteStorageConfig();
}
