package com.bx.research.constants;


import com.bx.research.BuildConfig;

public class ConstantsData {

    public static final int DEVICE_TYPE = 1;

    public static final String SP_FILE_NAME = "research_sp";

    public static final String DEFAULT_HOST;

    static {
        if (BuildConfig.DEBUG) {
//            DEFAULT_HOST = "http://192.168.1.104/";// 测试环境
//            DEFAULT_HOST = "http://www.mmouwang.com";// 正式环境
            DEFAULT_HOST = "http://zsw.frp.liwenbiao.com";// 正式环境
        } else {
//            DEFAULT_HOST = "http://192.168.1.104/";// 测试环境
            DEFAULT_HOST = "http://www.mmouwang.com";// 正式环境
//            DEFAULT_HOST = "http://zsw.frp.liwenbiao.com";// 测试环境
        }
    }

    public static final String APP_UPDATE = DEFAULT_HOST + "Version/Index";

    public static final String APP_MAIN_URL = DEFAULT_HOST + "/m";

    public static final String QQ_UNION = "https://graph.qq.com/oauth2.0/me";

    public static final String APP_SPLASH = DEFAULT_HOST + "/m/api/getSplash";


}
