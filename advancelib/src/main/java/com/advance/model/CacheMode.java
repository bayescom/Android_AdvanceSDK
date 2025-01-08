package com.advance.model;

public enum CacheMode {
    /**
     * 如果不设置，默认的缓存时间 3天
     */
    DEFAULT(3 * 24 * 60 * 60),
    /**
     * 短时间 3天
     */
    SHORT(3 * 24 * 60 * 60),
    /**
     * 一周时间
     */
    WEEK(7 * 24 * 60 * 60),
    /**
     * 一月时间
     */
    MONTH(30 * 24 * 60 * 60),
    /**
     * 不限定时间
     */
    UNLIMIT(-1);

    CacheMode(int ni) {
        savedTime = ni;
    }

    public int savedTime; //留存时间 单位秒
}
