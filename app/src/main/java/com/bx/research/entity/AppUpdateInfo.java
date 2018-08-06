package com.bx.research.entity;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by nate on 16/7/30.
 */
public class AppUpdateInfo extends BaseResult {
    //    {versionCode:20160730,versionName:"1.0",message:"修复了部分bug",url:"http://www.sjasjaksjkasd.apk"}

    private String versionCode;
    private String versionName;
    private String message;
    private String url;

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "AppUpdateInfo{" +
                "versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", message='" + message + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
