package com.bx.research.activity;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.bx.research.MainApplication;
import com.bx.research.R;
import com.bx.research.constants.ConstantsData;
import com.bx.research.entity.ShareInfo;
import com.bx.research.utils.PreferencesUtils;
import com.bx.research.utils.log.LogUtils;
import com.bx.research.widget.WinToast;
import com.google.gson.Gson;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.master.permissionhelper.PermissionHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @ViewInject(R.id.webView)
    private WebView mWebView;

    @ViewInject(R.id.loading_pb)
    public ProgressBar loading_pb;

    private String mUrl;
    private int mType;

    private ClipboardManager mClipboardManager;

    private PermissionHelper permissionHelper;

    private DownloadManager mDownloadManager;

//    @OnClick(R.id.btn_test)
//    public void testClick(View view) {
//
//
//    }


    @Override
    protected void onCreate() {
        super.onCreate();

        permissionHelper = new PermissionHelper(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);

        mClipboardManager = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);

        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        mType = getIntent().getIntExtra("type", -1);
        mUrl = getIntent().getStringExtra("url");
    }

    @Override
    protected void initView() {
        super.initView();

        setWebView(mWebView);

        try {
            mUrl = URLDecoder.decode(mUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mWebView.loadUrl(mUrl);

//        Beta.checkUpgrade();
        //登录之后过来的，需要检查更新
//        if (mType == 1) {
//            checkUpdate();
//        }

    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Uri result = (intent == null || resultCode != Activity.RESULT_OK) ? null : intent.getData();
        switch (requestCode) {
            case FILE_CHOOSER_RESULT_CODE:  //android 5.0以下 选择图片回调

                if (null == mUploadMessage)
                    return;
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;

                break;

            case FILE_CHOOSER_RESULT_CODE_FOR_ANDROID_5:  //android 5.0(含) 以上 选择图片回调

                if (null == mUploadMessageForAndroid5)
                    return;
                if (result != null) {
                    mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
                } else {
                    mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
                }
                mUploadMessageForAndroid5 = null;

                break;
        }
    }

    public ValueCallback<Uri[]> mUploadMessageForAndroid5;
    public ValueCallback<Uri> mUploadMessage;
    public final static int FILE_CHOOSER_RESULT_CODE_FOR_ANDROID_5 = 2;
    private final static int FILE_CHOOSER_RESULT_CODE = 1;// 表单的结果回调

    /**
     * 配置webView基本参数
     *
     * @param webView
     */
    private void setWebView(final WebView webView) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        String databasePath = mActivity.getDir("databases", Context.MODE_PRIVATE).getPath();
        webView.getSettings().setDatabasePath(databasePath);

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);

        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setDrawingCacheEnabled(true);

        //自适应屏幕
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        webView.addJavascriptInterface(new JsAction(), "ResearchJS");

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(message);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        result.confirm();
                    }
                });
                builder.show();

                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (null != loading_pb) {
                    loading_pb.setProgress(newProgress);
                    if (newProgress == 100) {
                        loading_pb.setVisibility(View.GONE);
                    }
                }
                super.onProgressChanged(view, newProgress);
            }

            // For Android < 5.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;

                openFileChooserImpl();
            }

            // For Android => 5.0
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg,
                                             WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadMessageForAndroid5 = uploadMsg;

                permissionHelper.request(new PermissionHelper.PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        onenFileChooseImpleForAndroid();
                    }

                    @Override
                    public void onIndividualPermissionGranted(String[] grantedPermission) {
                        LogUtils.d("onIndividualPermissionGranted() called with: grantedPermission = [" + TextUtils.join(",", grantedPermission) + "]");
                    }

                    @Override
                    public void onPermissionDenied() {
                        LogUtils.d("onPermissionDenied() called");
                    }

                    @Override
                    public void onPermissionDeniedBySystem() {
                        LogUtils.d("onPermissionDeniedBySystem() called");

                    }
                });

                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                LogUtils.d("=========onReceivedError=============");
            }
        });

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                if (TextUtils.isEmpty(url))
                    return;
                permissionHelper.request(new PermissionHelper.PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        downloadBySystem(url, "apk");
                    }

                    @Override
                    public void onIndividualPermissionGranted(String[] grantedPermission) {
                        LogUtils.d("onIndividualPermissionGranted() called with: grantedPermission = [" + TextUtils.join(",", grantedPermission) + "]");
                    }

                    @Override
                    public void onPermissionDenied() {
                        LogUtils.d("onPermissionDenied() called");
                    }

                    @Override
                    public void onPermissionDeniedBySystem() {
                        LogUtils.d("onPermissionDeniedBySystem() called");

                    }
                });
            }
        });

    }

    /**
     * android 5.0 以下开启图片选择（原生）
     * <p>
     * 可以自己改图片选择框架。
     */
    private void openFileChooserImpl() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "File Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * android 5.0(含) 以上开启图片选择（原生）
     * <p>
     * 可以自己改图片选择框架。
     */
    private void onenFileChooseImpleForAndroid() {
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");

        startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE_FOR_ANDROID_5);
    }

    /**
     * js方法
     */
    final class JsAction {
        JsAction() {
        }

        //打开新页面
        @JavascriptInterface
        public void start(String url, String isFinish) {
            LogUtils.d("=========start=============" + url);
            Intent intent = new Intent(mActivity, MainActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);

            if (isFinish.equals("1")) {
                finish();
            }
        }

        //关闭当前页面
        @JavascriptInterface
        public void close() {
            LogUtils.d("=========close=============");
            finish();
        }


        //分享
        @JavascriptInterface
        public void share(String info) {
            showShare(info);
        }

        //复制
        @JavascriptInterface
        public void copy(String content) {
            LogUtils.d("=========copy=============" + content);
            mClipboardManager.setText(content);
            WinToast.toast(mActivity, "已复制");
        }

        //退出登录
        @JavascriptInterface
        public void logout() {
            LogUtils.d("=========logout=============");
            int loginType = PreferencesUtils.getInt(mActivity, "loginType", -1);
            Platform platform = null;
            if (loginType == 1) {
                platform = ShareSDK.getPlatform(QQ.NAME);
            } else if (loginType == 2) {
                platform = ShareSDK.getPlatform(Wechat.NAME);
            }
            if (null != platform) {
                platform.removeAccount(true);
            }
            PreferencesUtils.clear(mActivity);
            MainApplication.getInstance().finishAllActivities();
            Intent intent = new Intent(mActivity, LoginActivity.class);
            intent.putExtra("closeBanner", true);
            startActivity(intent);
        }
    }

//    /**
//     * 检查更新
//     */
//    private void checkUpdate() {
//        Network.postNetwork(ConstantsData.APP_UPDATE, null, RequestParamsPostion.PARAMS_POSITION_BODY, new CallBack<AppUpdateInfo>(AppUpdateInfo.class) {
//            @Override
//            public void doSuccess(final AppUpdateInfo entity) {
//                LogUtils.d("=========AppUpdateInfo============" + entity.toString());
//                int currentVersion = getVersionCode(mActivity);
//                int serverVersion = Integer.parseInt(entity.getVersionCode());
//                if (serverVersion > currentVersion) {
//                    showUpdateDialog(entity);
//                }
//            }
//        });
//
//    }

//    /**
//     * 显示版本更新对话框
//     *
//     * @param entity
//     */
//    private void showUpdateDialog(final AppUpdateInfo entity) {
//        if (null != mActivity) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//            builder.setTitle("发现新版本");
//            builder.setMessage(entity.getMessage());
//            builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                }
//            });
//            builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    Intent intent = new Intent();
//                    intent.setAction("android.intent.action.VIEW");
//                    Uri content_url = Uri.parse(entity.getUrl());
//                    intent.setData(content_url);
//                    startActivity(intent);
//                }
//            });
//            builder.show();
//        }
//    }

//    /**
//     * 获取版本号
//     * 获取版本号(内部识别号)
//     *
//     * @param context
//     * @return
//     */
//    public static int getVersionCode(Context context) {
//        try {
//            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            return pi.versionCode;
//        } catch (PackageManager.NameNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            return 0;
//        }
//    }

    /**
     * 显示分享页面
     */
    private void showShare(String info) {
        LogUtils.d("===========info===========" + info);
        if (TextUtils.isEmpty(info)) {
            return;
        }

        Gson gson = new Gson();
        ShareInfo shareInfo = gson.fromJson(info, ShareInfo.class);
        LogUtils.d("======getUrl===========" + shareInfo.getUrl().replaceAll("&amp;", "&"));

        ShareSDK.initSDK(this);

        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(shareInfo.getTitle());
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(shareInfo.getUrl());
        // text是分享文本，所有平台都需要这个字段
        oks.setText(shareInfo.getText());
        oks.setImageUrl(shareInfo.getImageUrl().replaceAll("&amp;", "&"));
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        // oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(shareInfo.getUrl().replaceAll("&amp;", "&"));
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(shareInfo.getUrl());

        // 启动分享GUI
        oks.show(this);
    }

    /**
     * 下载文件
     *
     * @param url
     * @param mimeType
     */
    private void downloadBySystem(String url, String mimeType) {
        // 指定下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        // 设置通知栏的标题，如果不设置，默认使用文件名
//        request.setTitle("This is title");
        // 设置通知栏的描述
//        request.setDescription("This is description");
        // 允许在计费流量下下载
        request.setAllowedOverMetered(true);
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true);
        // 允许漫游时下载
        request.setAllowedOverRoaming(true);
        // 允许下载的网路类型
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        // 设置下载文件保存的路径和文件名
        String fileName = URLUtil.guessFileName(url, ConstantsData.APK_FILE_NAME, mimeType);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//        另外可选一下方法，自定义下载路径
//        request.setDestinationUri()
//        request.setDestinationInExternalFilesDir()
        // 添加一个下载任务
        long downloadId = mDownloadManager.enqueue(request);
    }

    @Override
    public void onBackPressed() {
        backPress();
    }

    /**
     * 返回
     */
    private void backPress() {
//        if (mWebView.canGoBack()) {
//            mWebView.goBack();
//        } else {
        finish();
//        }
    }
}
