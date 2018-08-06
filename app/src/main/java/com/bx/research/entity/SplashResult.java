package com.bx.research.entity;

public class SplashResult{


    /**
     * code : 1
     * msg : ok
     * data : {"imgUrl":"http://zsw.frp.liwenbiao.com/uploadPath/splash/20180804163518.jpg","href":"http://zsw.frp.liwenbiao.com/m/User/WelfareDetail/2","time":3000}
     */

    private int code;
    private String msg;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * imgUrl : http://zsw.frp.liwenbiao.com/uploadPath/splash/20180804163518.jpg
         * href : http://zsw.frp.liwenbiao.com/m/User/WelfareDetail/2
         * time : 3000
         */

        private String imgUrl;
        private String href;
        private int time;

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }
    }
}
