package com.bx.research.constants;


import com.bx.research.BuildConfig;

public class ConstantsData {

    public static final String SP_FILE_NAME = "research_sp";

    public static final String SDCARD_FILE_NAME = "research_file";

    public static final String DEFAULT_HOST;

    static {
        if (BuildConfig.DEBUG) {
//            DEFAULT_HOST = "http://192.168.1.14:8888/";// 测试环境
            DEFAULT_HOST = "http://2016.diaoyan360.com/";// 正式环境
        } else {
            DEFAULT_HOST = "http://2016.diaoyan360.com/";// 正式环境
        }
    }

    public static final String APP_UPDATE = DEFAULT_HOST + "Version/Index";

}
