package com.bx.research.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bx.research.R;
import com.bx.research.constants.ConstantsData;
import com.bx.research.utils.PreferencesUtils;
import com.bx.research.utils.log.LogUtils;
import com.bx.research.widget.WinToast;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity {

    @ViewInject(R.id.webView)
    private WebView mWebView;

    @ViewInject(R.id.loading_pb)
    public ProgressBar loading_pb;

    @ViewInject(R.id.iv_splash)
    public ImageView iv_splash;

    private boolean mIsLogin = false;//是否登录
    private int mLoginType = -1;//登录方式
    private String mUserInfo;//用户信息

    @OnClick(R.id.btn_test)
    public void testClick(View view) {
        shareSDKLogin(2);
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        mIsLogin = PreferencesUtils.getBoolean(mActivity, "isLogin", false);
        mLoginType = PreferencesUtils.getInt(mActivity, "loginType", -1);

        if (mIsLogin) {//已经登陆过，显示欢迎图片
            iv_splash.setVisibility(View.VISIBLE);
        } else {
            iv_splash.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initView() {
        super.initView();

        setWebView(mWebView);

        mWebView.loadUrl(ConstantsData.DEFAULT_HOST);
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
                if (mIsLogin) {
                    if (mLoginType == -1) {//手机登录
                        String phoneNumber = PreferencesUtils.getString(mActivity, "phoneNumber");
                        String js = "javascript:n_login(" + mLoginType + ",'" + phoneNumber + "','" + ConstantsData.DEVICE_TYPE + "')";
                        mWebView.loadUrl(js);
                    } else {
                        shareSDKLogin(mLoginType);
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                LogUtils.d("=========onReceivedError=============");
            }
        });
    }

    //登录后进入主界面需要检查更新
    private int mStartMainType = -1;

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
            intent.putExtra("type", mStartMainType);
            startActivity(intent);

            if (mStartMainType == 1) {
                mStartMainType = -1;
            }
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

        //登录成功
        @JavascriptInterface
        public void loginSuccess(String info) {
            LogUtils.d("=========loginSuccess=============" + info);
            PreferencesUtils.putBoolean(mActivity, "isLogin", true);
            PreferencesUtils.putInt(mActivity, "loginType", mLoginType);
            PreferencesUtils.putString(mActivity, "loginInfo", mUserInfo);
            PreferencesUtils.putString(mActivity, "phoneNumber", info);

            mStartMainType = 1;

            if (mLoginType > -1) {//第三方登录,自己实现跳转逻辑,//默认登录方式,调用 JS start跳转
                Intent intent = new Intent(mActivity, MainActivity.class);
                intent.putExtra("url", ConstantsData.APP_MAIN_URL);
                intent.putExtra("type", mStartMainType);
                startActivity(intent);
                finish();
            }
        }

        //登录
        @JavascriptInterface
        public void login(String type) {
            LogUtils.d("=========login=============" + type);
            mLoginType = Integer.parseInt(type);
            shareSDKLogin(mLoginType);
        }
    }

    public static final int READ_CONTACTS_REQUEST_CODE = 100;

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_CONTACTS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            shareSDKLogin(3);
        }
    }

    /**
     * 第三方登录
     *
     * @param type 1:qq  2:微信  3:微博
     */
    private void shareSDKLogin(int type) {
        mLoginType = type;
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
//            weChat.authorize();//单独授权
            weChat.showUser(null);//授权并获取用户信息
            //authorize与showUser单独调用一个即可
            //移除授权
            //weibo.removeAccount(true);
        } else {
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
                return;
            }

            Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
            weibo.setPlatformActionListener(mPlatformActionListener);
//            weibo.authorize();//单独授权
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
            //用户资源都保存到res //通过打印res数据看看有哪些数据是你想要的
            //通过DB获取各种数据
            if (mLoginType == 2) {
                mUserInfo = hashMap.toString().split("unionid=")[1].split(",")[0];
            } else {
                if (i == Platform.ACTION_USER_INFOR) {
                    PlatformDb platDB = platform.getDb();//获取数平台数据DB
                    mUserInfo = platDB.getToken();
                }
            }
            LogUtils.d("========mUserInfo=============" + mUserInfo);

            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String js = "javascript:n_login(" + mLoginType + ",'" + mUserInfo + "','" + ConstantsData.DEVICE_TYPE + "')";
                    mWebView.loadUrl(js);
                }
            });
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
        finish();
    }
}
