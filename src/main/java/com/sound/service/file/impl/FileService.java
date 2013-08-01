package com.sound.service.file.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.model.file.LocalSoundFile;
import com.sound.model.file.RemoteFile;

@Service
@Scope("singleton")
public class FileService implements com.sound.service.file.itf.FileService{

	@Override
	public RemoteFile upload(LocalSoundFile sound) {
		return null;
	}

	@Override
	public LocalSoundFile download(RemoteFile sound) {
		return null;
	}

}
