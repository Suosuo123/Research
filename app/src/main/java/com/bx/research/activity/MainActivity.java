package com.bx.research.activity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.bx.research.MainApplication;
import com.bx.research.R;
import com.bx.research.constants.ConstantsData;
import com.bx.research.entity.AppUpdateInfo;
import com.bx.research.entity.ShareInfo;
import com.bx.research.net.CallBack;
import com.bx.research.net.Network;
import com.bx.research.net.RequestParamsPostion;
import com.bx.research.utils.PreferencesUtils;
import com.bx.research.utils.log.LogUtils;
import com.bx.research.widget.WinToast;
import com.google.gson.Gson;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
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

    @OnClick(R.id.btn_test)
    public void testClick(View view) {
        String info = "{'title': '测试问卷','text': '内容文本','imageUrl': 'http://pic.qqtn.com/up/2016-7/2016072614451378952.jpg','url': 'http://2016.diaoyan360.com/InversTask/TaskDetail?taskId=12&uid=30BB99FA78FF9423'}";
        showShare(info);
    }


    @Override
    protected void onCreate() {
        super.onCreate();

        mClipboardManager = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);

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

        //登录之后过来的，需要检查更新
        if (mType == 1) {
            checkUpdate();
        }

    }

    @Override
    protected void initData() {
        super.initData();
    }

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
            LogUtils.d("=========start=============" + url + "=======" + isFinish);
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
            Platform platform;
            if (loginType == 1) {
                platform = ShareSDK.getPlatform(QQ.NAME);
            } else if (loginType == 2) {
                platform = ShareSDK.getPlatform(Wechat.NAME);
            } else {
                platform = ShareSDK.getPlatform(SinaWeibo.NAME);
            }
            if (null != platform) {
                platform.removeAccount(true);
            }

            PreferencesUtils.putBoolean(mActivity, "isLogin", false);
            PreferencesUtils.putInt(mActivity, "loginType", -1);
            PreferencesUtils.putString(mActivity, "loginInfo", "");
            PreferencesUtils.putString(mActivity, "phoneNumber", "");

            MainApplication.getInstance().finishAllActivities();
//            Intent intent = new Intent(mActivity, LoginActivity.class);
//            startActivity(intent);


        }
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        Network.postNetwork(ConstantsData.APP_UPDATE, null, RequestParamsPostion.PARAMS_POSITION_BODY, new CallBack<AppUpdateInfo>(AppUpdateInfo.class) {
            @Override
            public void doSuccess(final AppUpdateInfo entity) {
                LogUtils.d("=========AppUpdateInfo============" + entity.toString());
                int currentVersion = getVersionCode(mActivity);
                int serverVersion = Integer.parseInt(entity.getVersionCode());
                if (serverVersion > currentVersion) {
                    showUpdateDialog(entity);
                }
            }
        });

    }

    /**
     * 显示版本更新对话框
     *
     * @param entity
     */
    private void showUpdateDialog(final AppUpdateInfo entity) {
        if (null != mActivity) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle("发现新版本");
            builder.setMessage(entity.getMessage());
            builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(entity.getUrl());
                    intent.setData(content_url);
                    startActivity(intent);
                }
            });
            builder.show();
        }
    }

    /**
     * 获取版本号
     * 获取版本号(内部识别号)
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
    }

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
