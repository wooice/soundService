package com.sound.service.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.sound.exception.RemoteStorageException;
import com.sound.model.enums.FileType;
import com.sound.service.storage.impl.RemoteStorageService;

public class RemoteStorageServiceTest extends TestCase {
	private static final String TestFileName = "test-file";

	private com.sound.service.storage.itf.RemoteStorageService remoteStorageService;

	protected void setUp() throws Exception {
		super.setUp();
		remoteStorageService = new RemoteStorageService();
	}

	public void testUpload() {
		File file = new File(TestFileName);
		try {
			file.createNewFile();
			remoteStorageService.upload(file, FileType.SOUND);
		} catch (RemoteStorageException e) {
			e.printStackTrace();
			fail("Upload error");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Cannot create local file");
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	public void testDownloadToFile() {
		try {
			remoteStorageService.downloadToFile(TestFileName, TestFileName
					+ ".local", FileType.SOUND);
			File local = new File(TestFileName + ".local");
			Assert.assertTrue(local.exists());
		} catch (RemoteStorageException e) {
			e.printStackTrace();
			fail("Error download to File");
		} finally {
			File f = new File(TestFileName + ".local");
			if (f.exists()) {
				f.delete();
			}
		}

	}

	public void testDownloadToMemory() {
		InputStream is = null;
		try {
			is = remoteStorageService.downloadToMemory(TestFileName,
					FileType.SOUND);
			Assert.assertNotNull(is);
		} catch (RemoteStorageException e) {
			e.printStackTrace();
			fail("Error download to memory");
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public void testDelete() {
		try {
			remoteStorageService.delete(TestFileName, FileType.SOUND);
		} catch (RemoteStorageException e) {
			e.printStackTrace();
			fail("Delete error");
		}
	}

}
