package com.sound.service.file.itf;

import com.sound.model.file.LocalSoundFile;
import com.sound.model.file.RemoteFile;

public interface FileService {

	public RemoteFile upload(LocalSoundFile sound);

	public LocalSoundFile download(RemoteFile sound);

}
