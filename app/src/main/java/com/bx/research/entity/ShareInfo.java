package com.bx.research.entity;

/**
 * Created by nate on 16/7/30.
 */
public class ShareInfo extends BaseEntity {
//    “title”:标题
//    “text”:内容
//    “imageUrl”:图片地址
//    “url”:点击后的链接

    private String title;
    private String text;
    private String imageUrl;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ShareInfo{" +
                "title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
