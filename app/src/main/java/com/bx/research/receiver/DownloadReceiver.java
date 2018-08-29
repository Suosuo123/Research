package com.bx.research.receiver;


import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.bx.research.MainApplication;

import java.util.HashSet;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * @description 下载完成广播接收器
 */
public class DownloadReceiver extends BroadcastReceiver {

    private HashSet<Long> mCompleteSet = new HashSet();

    private DownloadManager mDownloadManager;

    private NotificationManager mNotifyManager;

    public DownloadReceiver() {
        super();

        mDownloadManager = (DownloadManager) MainApplication.getInstance().getSystemService(DOWNLOAD_SERVICE);
        mNotifyManager = (NotificationManager) MainApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //在广播中取出下载任务的id
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            mCompleteSet.add(id);
            openInstall(id);
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
            if (mCompleteSet.contains(id)) {
                openInstall(id);
            }
        }
    }

    private void openInstall(long id) {
        Uri uri = mDownloadManager.getUriForDownloadedFile(id);
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        MainApplication.getInstance().startActivity(i);

        mNotifyManager.cancel((int) id);
    }

}
