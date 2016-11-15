package com.sound.constant;

import java.io.File;

import com.sound.model.User.UserRole;

public class Constant {
  public static final String UPLOAD_QUEUE_FOLDER = System.getProperty("java.io.tmpdir")
      + File.separator + "WOOICE" + File.separator + "QUEUE";

  public static final String UNIFORM_SOUND_TYPE = "mp3";

  public static final String GUEST_ROLE = "guest";

  public static final String USER_ROLE = "user";
  
  public static final UserRole USER_ROLE_OBJ = new UserRole(USER_ROLE);
  
  public static final String PRO_ROLE = "pro";
  
  public static final UserRole PRO_ROLE_OBJ = new UserRole(PRO_ROLE);
  
  public static final String SPRO_ROLE = "spro";
  
  public static final UserRole SPRO_ROLE_OBJ = new UserRole(SPRO_ROLE);

  public static final String ADMIN_ROLE = "admin";
  
  public static final UserRole ADMIN_ROLE_OBJ = new UserRole(ADMIN_ROLE);

  public static final String SOUND_RECORD_CREATE = "create";

  public static final String SOUND_RECORD_REPOST = "repost";
  
  public static final String DEFAULT_USER_AVATOR = "img/default_avatar.png";
  
  public static final Integer USER_ALLOWED_DURATION = 120;
  
  public static final Integer PRO_ALLOWED_DURATION = 360;
  
  public static final Integer WEEKLY_ALLOWED_DURATION = 20 * 60;
 
  public static final String COMMENT_PUBLIC = "public";
  
  public static final String COMMENT_PRIVATE = "private";
  
  public static final String COMMENT_CLOSED = "closed";
  
  public static final int SOUND_NORMAL = 0;
  
  public static final int SOUND_HIGHLIGHT = 1;

  public static final int REPORTS_LIMIT = 30;
  
  public static final int SOUND_CACHE_TIME = 7 * 24 * 60 * 60 * 1000;
}
