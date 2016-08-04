package com.bx.research;

import android.app.Activity;
import android.app.Application;

import com.lidroid.xutils.DbUtils;

import java.util.LinkedList;
import java.util.List;

public class MainApplication extends Application {
    private static MainApplication mContext;
    private DbUtils db;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
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
    private List<Activity> mList = new LinkedList<Activity>();

    // add Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    // 关闭每一个list内的activity
    public void finishAllActivities() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
