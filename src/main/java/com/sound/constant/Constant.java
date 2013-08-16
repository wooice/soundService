package com.sound.constant;

import java.io.File;

public class Constant {

	public static final String UPLOAD_TEMP_FOLDER = System.getProperty("java.io.tmpdir") + File.separator + "WOOICE" + File.separator + "UPLOAD";
	
	public static final String UPLOAD_QUEUE_FOLDER = System.getProperty("java.io.tmpdir") + File.separator +  "WOOICE" + File.separator + "QUEUE";

}
