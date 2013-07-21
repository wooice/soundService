package com.sound.service.storage.itf;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.sound.dto.storage.GetFileRequest;
import com.sound.dto.storage.PutFileRequest;
import com.sound.exception.RemoteStorageException;

public interface RemoteStorageService {
	
	public List<String> listOwnedFiles(String ownerId) throws RemoteStorageException;
	
	public void downloadToFile(String fileName, String fullPath) throws RemoteStorageException;
	
	public InputStream downloadToMemory(String fileName) throws RemoteStorageException;
	
	public void upload(File file) throws RemoteStorageException;
	
	public void delete(String fileName) throws RemoteStorageException;
	
	public PropertiesConfiguration getRemoteStorageConfig();

	public URL generateDownloadUrl(String file);

	public URL generateUploadUrl(String file);
	
	public PutFileRequest contructPutReuqest(String type, String fileName);
	
	public GetFileRequest contructGetReuqest(String fileName);
}
