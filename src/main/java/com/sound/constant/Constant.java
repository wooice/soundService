package com.sound.constant;

import java.io.File;

public class Constant {

  public static final String UPLOAD_TEMP_FOLDER = System.getProperty("java.io.tmpdir")
      + File.separator + "WOOICE" + File.separator + "UPLOAD";

  public static final String UPLOAD_QUEUE_FOLDER = System.getProperty("java.io.tmpdir")
      + File.separator + "WOOICE" + File.separator + "QUEUE";

  public static final String UNIFORM_SOUND_TYPE = "mp3";

  public static final String GUEST_ROLE = "guest";

  public static final String USER_ROLE = "user";

  public static final String ADMIN_ROLE = "admin";
}
