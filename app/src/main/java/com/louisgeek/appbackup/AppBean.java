package com.louisgeek.appbackup;

import android.graphics.drawable.Drawable;

/**
 * Created by classichu on 2018/3/16.
 */

public class AppBean {
    public static final int TYPE_USER = 3;
    public static final int TYPE_UPDATED_SYSTEM = 2;
    public static final int TYPE_SYSTEM = 1;
    public String mName;
    public String mPackageName;
    public String versionName;
    public int versionCode;
    public String mPath;
    public Drawable mDrawable;
    public int type;
}
