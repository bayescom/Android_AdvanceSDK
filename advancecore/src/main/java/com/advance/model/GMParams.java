package com.advance.model;

import java.util.ArrayList;

//后端下发的gm信息类
public class GMParams {
    public String appid = "";
    public String adspotid = "";
    public int timeout; //超时时长，单位毫秒，目前仅开屏位置生效。后端可能不返回该字段

    //v4.1.1新增
    public ArrayList<String> imptk;
    public ArrayList<String> clicktk;
    public ArrayList<String> succeedtk;
    public ArrayList<String> failedtk;
    public ArrayList<String> loadedtk;
}
