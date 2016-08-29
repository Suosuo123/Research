package com.bx.research.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.bx.research.entity.UserInfo;
import com.bx.research.net.CallBack;
import com.bx.research.net.Network;
import com.bx.research.net.RequestParamsPostion;
import com.bx.research.utils.SharedPreferenceUtils;
import com.bx.research.utils.log.LogUtils;
import com.bx.research.widget.WinToast;
import com.google.gson.Gson;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
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

    private DbUtils mDbUtils;

    @OnClick(R.id.btn_share)
    public void share(View view) {
        showShare();
    }

    @OnClick(R.id.btn_qq)
    public void qq(View view) {
        shareSDKLogin(1);
    }

    @OnClick(R.id.btn_wechat)
    public void weChat(View view) {
        shareSDKLogin(2);
    }

    @OnClick(R.id.btn_sina)
    public void sina(View view) {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
            return;
        }

        shareSDKLogin(3);
    }

    public static final int READ_CONTACTS_REQUEST_CODE = 100;

    @Override
    protected void onCreate() {
        super.onCreate();

        mDbUtils = MainApplication.getInstance().getDbUtils();

        mUrl = getIntent().getStringExtra("url");
    }

    @Override
    protected void initView() {
        super.initView();

        setWebView(mWebView);

        //登录页面
        if (TextUtils.isEmpty(mUrl)) {
            String id = SharedPreferenceUtils.getString("id");
            UserInfo userInfo = null;
            try {
                userInfo = mDbUtils.findById(UserInfo.class, id);
            } catch (DbException e) {
                e.printStackTrace();
            }
            if (userInfo != null) {//说明已经登陆过
                mWebView.loadUrl(ConstantsData.DEFAULT_HOST + "?id=" + userInfo.getId());
            } else {
                mWebView.loadUrl(ConstantsData.DEFAULT_HOST);
            }

            checkUpdate();

        } else {
            try {
                mUrl = URLDecoder.decode(mUrl, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mWebView.loadUrl(mUrl);

        }

    }

    @Override
    protected void initData() {
        super.initData();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_CONTACTS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            shareSDKLogin(3);
        } else {

        }
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

        //获取用户的登录信息
        @JavascriptInterface
        public void passUserInfo(String info) {
            Gson gson = new Gson();
            UserInfo userInfo = gson.fromJson(info, UserInfo.class);

            SharedPreferenceUtils.putString("id", userInfo.getId());
            //保存至数据库
            try {
                mDbUtils.save(userInfo);
            } catch (DbException e) {
                e.printStackTrace();
            }
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
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context)//获取版本号(内部识别号)
    {
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
    private void showShare() {
        ShareSDK.initSDK(this);

        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle("标题");
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("我是分享文本");
        oks.setImageUrl("http://v1.qzone.cc/avatar/201309/25/13/12/524270bb2353a904.jpg%21200x200.jpg");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        // oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

// 启动分享GUI
        oks.show(this);
    }

    /**
     * 第三方登录
     *
     * @param type 1:qq  2:微信  3:微博
     */
    private void shareSDKLogin(int type) {

        ShareSDK.initSDK(this);

        if (type == 1) {
            Platform qq = ShareSDK.getPlatform(QQ.NAME);
            qq.setPlatformActionListener(mPlatformActionListener);
//        qq.authorize();//单独授权
            qq.showUser(null);//授权并获取用户信息
            //authorize与showUser单独调用一个即可
            //移除授权
            //weibo.removeAccount(true);
        } else if (type == 2) {
            Platform weChat = ShareSDK.getPlatform(Wechat.NAME);
            weChat.setPlatformActionListener(mPlatformActionListener);
            weChat.authorize();//单独授权
            weChat.showUser(null);//授权并获取用户信息
            //authorize与showUser单独调用一个即可
            //移除授权
            //weibo.removeAccount(true);
        } else {
            Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
            weibo.setPlatformActionListener(mPlatformActionListener);
            weibo.authorize();//单独授权
            weibo.showUser(null);//授权并获取用户信息
            //authorize与showUser单独调用一个即可
            //移除授权
            //weibo.removeAccount(true);
        }
    }

    /**
     * 第三方登录回调
     */
    private PlatformActionListener mPlatformActionListener = new PlatformActionListener() {
        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            LogUtils.d("=====onComplete=======" + hashMap.toString());

            Looper.prepare();
            new AlertDialog.Builder(mActivity).setMessage(hashMap.toString()).setTitle("获取到用户信息").show();
            Looper.loop();

        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            LogUtils.d("=====onError=======" + throwable.toString());
        }

        @Override
        public void onCancel(Platform platform, int i) {
            LogUtils.d("=====onCancel=======");
        }
    };

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
