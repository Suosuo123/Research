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
import android.text.TextUtils;
import android.util.Log;
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
import com.bx.research.entity.AppUpdateInfo;
import com.bx.research.net.CallBack;
import com.bx.research.net.Network;
import com.bx.research.net.NetworkUtils;
import com.bx.research.net.RequestParamsPostion;
import com.bx.research.utils.PreferencesUtils;
import com.bx.research.utils.log.LogUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
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

    @Override
    protected void onCreate() {
        super.onCreate();

        ShareSDK.initSDK(mActivity);

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

        mWebView.loadUrl(ConstantsData.APP_MAIN_URL);
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
                LogUtils.d("=======onJsAlert");
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
                        String js = "javascript:window.app.n_login(" + mLoginType + "," + ConstantsData.DEVICE_TYPE + "," + PreferencesUtils.getString(mActivity, "userId") + "," + PreferencesUtils.getString(mActivity, "userInfo") + ")";
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
            LogUtils.d("=========start=============" + url);
            LogUtils.d("=========isFinish=============" + isFinish);
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
        public void loginSuccess(String userId) {
            LogUtils.d("=========loginSuccess=============" + userId);

            mStartMainType = 1;

            PreferencesUtils.putBoolean(mActivity, "isLogin", true);
            PreferencesUtils.putInt(mActivity, "loginType", mLoginType);

            PreferencesUtils.putString(mActivity, "userId", userId);


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
            if (mLoginType == 1) {
                Platform qq = ShareSDK.getPlatform(QQ.NAME);
                qq.removeAccount(true);
            } else if (mLoginType == 2) {
                Platform weChat = ShareSDK.getPlatform(Wechat.NAME);
                weChat.removeAccount(true);
            }

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
        if (type == 1) {
            Platform qq = ShareSDK.getPlatform(QQ.NAME);
            if (qq.isAuthValid()) {
                thirdLoginSuccess(qq);
                return;
            }
            qq.setPlatformActionListener(mPlatformActionListener);
            qq.showUser(null);//授权并获取用户信息
        } else if (type == 2) {
            Platform weChat = ShareSDK.getPlatform(Wechat.NAME);
            if (weChat.isAuthValid()) {
                thirdLoginSuccess(weChat);
                return;
            }
            weChat.setPlatformActionListener(mPlatformActionListener);
            weChat.showUser(null);//授权并获取用户信息
        }
//        else {
//            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
//                //申请WRITE_EXTERNAL_STORAGE权限
//                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
//                return;
//            }
//
//            Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
//            weibo.setPlatformActionListener(mPlatformActionListener);
////            weibo.authorize();//单独授权
//            weibo.showUser(null);//授权并获取用户信息
//            //authorize与showUser单独调用一个即可
//            //移除授权
//            //weibo.removeAccount(true);
//        }
    }

    /**
     * 第三方登录回调
     */
    private PlatformActionListener mPlatformActionListener = new PlatformActionListener() {
        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            //用户资源都保存到res //通过打印res数据看看有哪些数据是你想要的
            //通过DB获取各种数据
//            if (mLoginType == 2) {
//                mUserInfo = hashMap.toString().split("unionid=")[1].split(",")[0];
//            } else {
//                if (i == Platform.ACTION_USER_INFOR) {
//                    PlatformDb platDB = platform.getDb();//获取数平台数据DB
//                    mUserInfo = platDB.getToken();
//                }
//            }

            thirdLoginSuccess(platform);
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

    /**
     * 第三方登录成功
     *
     * @param platform
     */
    private void thirdLoginSuccess(Platform platform) {
        final String userInfo = platform.getDb().exportData().toString();
        LogUtils.d("=====onComplete=======" + userInfo);

        //qq登录，先获取unionID
        if (platform.getName().equals(QQ.NAME)) {
            Map<String, String> params = new HashMap<>();
            params.put("access_token", platform.getDb().getToken());
            params.put("unionid", "1");
            Network.postNetwork(ConstantsData.QQ_UNION, params, RequestParamsPostion.PARAMS_POSITION_BODY, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    if (!TextUtils.isEmpty(responseInfo.result)) {
                        String result = (userInfo + responseInfo.result).replace(" ", "").trim();
                        final String totalResult = result.replace("}callback({", ",").replace(");", "");

                        PreferencesUtils.putString(mActivity, "userInfo", totalResult);

                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String js = "javascript:window.app.n_login(" + mLoginType + "," + ConstantsData.DEVICE_TYPE + ",'" + PreferencesUtils.getString(mActivity, "userId", "") + "'," + totalResult + ")";
                                LogUtils.d("=========js=====" + js);
                                mWebView.loadUrl(js);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {

                }
            });
        } else {
            PreferencesUtils.putString(mActivity, "userInfo", platform.getDb().exportData().toString());

            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String js = "javascript:window.app.n_login(" + mLoginType + "," + ConstantsData.DEVICE_TYPE + "," + PreferencesUtils.getString(mActivity, "userId") + "," + userInfo + ")";
                    mWebView.loadUrl(js);
                }
            });
        }

    }

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
