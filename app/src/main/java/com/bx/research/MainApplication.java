package com.bx.research;

import android.app.Activity;
import android.app.Application;

import com.lidroid.xutils.DbUtils;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.LinkedList;
import java.util.List;

public class MainApplication extends Application {
    private static MainApplication mContext;
    private DbUtils db;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

//        CrashReport.initCrashReport(getApplicationContext(), "ae88d0bf8b", false);
        Bugly.init(getApplicationContext(), "ae88d0bf8b", false);
    }

    public static MainApplication getInstance() {
        return mContext;
    }

    public DbUtils getDbUtils() {
        if (db == null) {
            db = DbUtils.create(mContext);
            db.configAllowTransaction(true);
            db.configDebug(true);
        }
        return db;
    }

    // 运用list来保存们每一个activity是关键
    private List<Activity> mActivityList = new LinkedList<Activity>();

    /**
     * add Activity
     *
     * @param activity
     */
    public void addActivity(Activity activity) {
        mActivityList.add(activity);
    }

    /**
     * remove Activity
     *
     * @param activity
     */
    public void removeActivity(Activity activity) {
        mActivityList.remove(activity);
    }

    // 关闭每一个list内的activity
    public void finishAllActivities() {
        try {
            for (Activity activity : mActivityList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
