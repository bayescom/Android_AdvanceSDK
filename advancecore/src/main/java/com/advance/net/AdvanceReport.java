package com.advance.net;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.advance.AdvanceConfig;
import com.advance.AdvanceConstant;
import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.BaseSetting;
import com.advance.model.AdvanceError;
import com.advance.model.AdvanceReportModel;
import com.advance.model.AdvanceSDKCacheModel;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.net.BYNetRequest;
import com.bayes.sdk.basic.net.BYReqCallBack;
import com.bayes.sdk.basic.net.BYReqModel;
import com.bayes.sdk.basic.trackretry.BYTrackClassifier;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.bayes.sdk.basic.util.BYCatch;

import org.json.JSONObject;

import java.util.ArrayList;

//上报相关处理
public class AdvanceReport {
    // TODO: 2023/5/24 上报如何保证去重效果？目前多次点击会出现多次上报
    public static void reportToUrls(final ArrayList<String> reportList) {
        reportToUrls(reportList, false);
    }

    public static void reportToUrls(final ArrayList<String> reportList, boolean needDelay) {
        try {
            if (reportList == null) {
                return;
            }
            for (int i = 0; i < reportList.size(); i++) {
                String urlStringTemp = reportList.get(i);
                final String urlString = urlStringTemp.replace("__TIME__", "" + System.currentTimeMillis());

                int delay = AdvanceConfig.getInstance().getReportDelayTime();
                if (delay > 0 && needDelay) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startReport(urlString);
                        }
                    }, delay);
                } else {
                    startReport(urlString);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    //    开始进行上报处理
    public static void startReport(final String urlString) {
        startReport(urlString, "");
    }

    /**
     * 带来源 tag 的上报。from 用于区分事件类型：
     * 含 click / imp / win（或中文 点击 / 展示 / 竞胜）的"计费/对账类"事件走 L0 先落库模型（prepare/ack），
     * 其余普通事件按 L1 处理。未开启补发时 prepare/ack/onReportFailed 均为空操作，行为不变。
     */
    public static void startReport(final String urlString, final String from) {
        try {
            BYReqModel request = new BYReqModel();
            final String finalUrl = urlString;   // 关键：宏替换在调用方已完成，此处 url 即唯一键
            request.reqUrl = finalUrl;
            request.timeoutMs = 10000;
            // 重要事件（计费/对账类）先落库：曝光/点击/竞胜
            boolean isMajor = false;
            try {
                String low = from == null ? "" : from.toLowerCase();
                isMajor = low.contains("click") || low.contains("imp") || low.contains("win")
                        || low.contains("点击") || low.contains("展示") || low.contains("竞胜");
                if (isMajor) {
                    TrackRetryManager.prepare(finalUrl);   // 返回 null 表示补发未开启，空操作
                }
            } catch (Throwable t) {
                BYCatch.onCaught(t, false, BYCatch.LEVEL_WARN);
            }
            final boolean finalIsMajor = isMajor;
            //get请求 会自动重定向，上报不成功尝试重试上报
            BYNetRequest.get(request, new BYReqCallBack() {
                @Override
                public void onSuccess(String s) {
                    String devTips = "上报成功,url=" + finalUrl + ",msg=" + s;
                    LogUtil.devDebugAuto(devTips, "上报成功");
                    if (finalIsMajor) {
                        TrackRetryManager.ack(finalUrl);   // 首次发送成功 → 出池
                    }
                }

                @Override
                public void onFailed(int i, String s) {
                    LogUtil.devDebug("上报失败, " + "code = " + i + ", msg = " + s);

                    // 失败分类：永久错误（坏 URL / 重定向超限 / HTTP 4xx，429 除外）
                    // 连补发池都不进，避免白烧重试次数与磁盘 blob。
                    if (BYTrackClassifier.isBadUrl(finalUrl)
                            || BYTrackClassifier.isPermanentFailure(i, s)) {
                        LogUtil.w(  "[AdvanceReport] 永久错误，不入补发池 url=" + finalUrl);
                        return;
                    }
                    // 失败 → 入待补发池（ToBid 模型：失败才落库）。
                    // 注意：补发能力默认关闭，服务端下发 tk_retry_config 且 enable=1 才生效，
                    // 未开启时这里是空操作，既有行为完全不变。
                    TrackRetryManager.onReportFailed(finalUrl, from);
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 新的异常上报方法 v3.4.4（不包含）以后可用
     */
    public static void newPackageError(Context context, AdvanceReportModel req) {
        try {
            if (req == null) {
                LogUtil.e("newPackageError req null");
                return;
            }
            String time = System.currentTimeMillis() + "";
            String reqid = req.reqid;
            //如果不存在，手动生成一个
            if (TextUtils.isEmpty(reqid)) {
                reqid = BYUtil.getUUID();
            }

            final JSONObject object = new JSONObject();

            object.putOpt("sdkver", AdvanceConfig.AdvanceSdkVersion);
            object.putOpt("sdktag", 0);
            object.putOpt("appver", BYDevice.getAppVersionValue());
//            object.putOpt("timestamp", time);
            object.putOpt("reqid", reqid);
            object.putOpt("adspotId", req.adspotid);
            object.putOpt("sdkadspotid", req.supadspotid);
            if (!TextUtils.isEmpty(req.ext)) {
                object.putOpt("ext", req.ext);
            }

            object.putOpt("code", req.code);
            object.putOpt("msg", req.msg);
            if (req.status >= 0) {
                object.putOpt("status", req.status);
            }

            int delay = AdvanceConfig.getInstance().getReportDelayTime();
            if (delay > 0 && req.needDelay) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startErrReport(object);
                    }
                }, delay);
            } else {
                startErrReport(object);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static void startErrReport(final JSONObject request) {
        try {
            BYReqModel reqModel = new BYReqModel();
            if (AdvanceSetting.getInstance().useHttps) {
                reqModel.reqUrl = AdvanceConfig.SDK_ERR_REPORT_URL_HTTPS;
            } else {
                reqModel.reqUrl = AdvanceConfig.SDK_ERR_REPORT_URL;
            }
            reqModel.timeoutMs = 10000;
            reqModel.reqBody = request;
            //尽可能避免基础库版本低找不到变量问题
//            try {
//                reqModel.forceGzip = true;
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
            BYNetRequest.post(reqModel, new BYReqCallBack() {
                @Override
                public void onSuccess(String s) {
                    LogUtil.devDebug("startErrReport 成功，");
                }

                @Override
                public void onFailed(int i, String s) {
                    LogUtil.devDebug("startErrReport 失败，code：" + i + ",msg:" + s);
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getBidReplacedImp(ArrayList<String> originalTk, long requestTime, String newReqId, double bidPrice) {
        try {
            long time = System.currentTimeMillis() - requestTime;
            String loadedMsg = "tt_" + time;
            LogUtil.high("广告请求到展示总耗时：" + time + "ms");
            return recordForReport(originalTk, loadedMsg, newReqId, bidPrice);
        } catch (Throwable e) {
            e.printStackTrace();
            return originalTk;
        }
    }

    public static ArrayList<String> getReplacedImp(ArrayList<String> originalTk, long requestTime, String newReqId) {
        try {
            long time = System.currentTimeMillis() - requestTime;
            String loadedMsg = "tt_" + time;
            LogUtil.high("广告请求到展示总耗时：" + time + "ms");
            return recordForReport(originalTk, loadedMsg, newReqId);
        } catch (Throwable e) {
            e.printStackTrace();
            return originalTk;
        }
    }

    public static ArrayList<String> getReplacedLoaded(ArrayList<String> originalTk, long requestTime, String newReqId) {
        try {
            long time = System.currentTimeMillis() - requestTime;
            String loadedMsg = "l_" + time;
            LogUtil.high("聚合启动到SDK启动耗时：" + time + "ms");
            return recordForReport(originalTk, loadedMsg, newReqId);
        } catch (Throwable e) {
            e.printStackTrace();
            return originalTk;
        }
    }

    public static ArrayList<String> getReplacedFailed(ArrayList<String> originalTk, AdvanceError advanceError, String newReqId) {
        try {
            String failedMsg;
            if (advanceError != null) {
                failedMsg = "err_" + advanceError.code;
                LogUtil.e(advanceError.toString());
                return recordForReport(originalTk, failedMsg, newReqId);
            } else {
                return originalTk;
            }

        } catch (Throwable e) {
            e.printStackTrace();
            return originalTk;
        }
    }

    /**
     * 记录一些数据，拼接在上报链接的尾部
     *
     * @return 拼接后的上报地址
     */
    public static ArrayList<String> recordForReport(ArrayList<String> originalTk, String msg, String newReqId) {
        return recordForReport(originalTk, msg, newReqId, -1);
    }

    private static ArrayList<String> recordForReport(ArrayList<String> originalTk, String msg, String newReqId, double bidPrice) {
        try {
            ArrayList<String> result = new ArrayList<>();

            if (originalTk != null && originalTk.size() > 0) {
                for (String tk : originalTk) {
                    if (tk != null && !"".equals(tk)) {
                        tk = tk.replace("__TIME__", "" + System.currentTimeMillis());
                        if (tk.contains("track_time")) {
//                            tk = tk.replace("track_time", "t_msg=" + msg + "&track_time");
                            tk = tk + "&t_msg=" + msg;
                        }
                        if (!TextUtils.isEmpty(newReqId)) {
                            tk = replaceParameter(tk, AdvanceConstant.URL_REQID_TAG, newReqId);
                        }
                        if (bidPrice > 0) {
                            tk = tk + "&bidResult=" + bidPrice;
                        }
                    }
                    result.add(tk);
                }
            }
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            return originalTk;
        }
    }


    /**
     * 记录一些数据，拼接在上报链接的尾部
     *
     * @return 拼接后的上报地址
     */
    public static ArrayList<String> getReplacedBidding(ArrayList<String> originalTk, String newReqId, double price) {
        try {
            ArrayList<String> result = new ArrayList<>();

            if (originalTk != null && originalTk.size() > 0) {
                for (String tk : originalTk) {
                    if (tk != null && !"".equals(tk)) {
                        tk = tk.replace("__TIME__", "" + System.currentTimeMillis());
                        if (!TextUtils.isEmpty(newReqId)) {
                            tk = replaceParameter(tk, AdvanceConstant.URL_REQID_TAG, newReqId);
                        }
                        //上报bid信息
                        if (price > 0) {
                            tk = tk + "&bidResult=" + price;
                        }
                    }
                    result.add(tk);
                }
            }
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            return originalTk;
        }
    }

    /**
     * 记录一些数据，拼接在上报链接的尾部
     *
     * @return 拼接后的上报地址
     */
    public static ArrayList<String> getReplacedTime(ArrayList<String> originalTk, String newReqId) {
        try {
            ArrayList<String> result = new ArrayList<>();

            if (originalTk != null && originalTk.size() > 0) {
                for (String tk : originalTk) {
                    if (tk != null && !"".equals(tk)) {
                        tk = tk.replace("__TIME__", "" + System.currentTimeMillis());
                        if (!TextUtils.isEmpty(newReqId)) {
                            tk = replaceParameter(tk, AdvanceConstant.URL_REQID_TAG, newReqId);
                        }
                    }
                    result.add(tk);
                }
            }
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            return originalTk;
        }
    }

    public static String reportReplacedCommon(String tk, BaseParallelAdapter adapter, String tag) {
        try {
            if (BYStringUtil.isNotEmpty(tk) && adapter != null) {
                BaseSetting baseSetting = adapter.baseSetting;
                String reqid = baseSetting == null ? "" : baseSetting.getAdvanceId();
                long reqTime = baseSetting == null ? 0 : baseSetting.getRequestTime();
                long cost = System.currentTimeMillis() - reqTime;
                LogUtil.high("聚合启动到" + tag + "耗时：" + cost + "ms");
                tk = tk.replace("__TIME__", "" + System.currentTimeMillis());
                //添加时长
                if (tk.contains("track_time")) {
                    tk = tk + "&t_msg=l_" + cost;
                }
                //替换reqid 后
                if (!TextUtils.isEmpty(reqid)) {
                    tk = replaceParameter(tk, AdvanceConstant.URL_REQID_TAG, reqid);
                }
                tk = replaceCachedInf(tk, adapter.getCacheModel());
                AdvanceReport.startReport(tk, tag);
                return tk;
            }
        }  catch (Throwable e) {
            e.printStackTrace();
        }
        return tk;
    }

    // 存在缓存时，额外增加埋点信息上报
    public static String replaceCachedInf(String ori, AdvanceSDKCacheModel cacheModel) {
        if (cacheModel != null) {
            String cachedReqID = cacheModel.serverReqID;
            ori = ori + "&is_cached=1&cached_reqid=" + cachedReqID;
        }
        return ori;
    }

    public static void replaceReportSuccess(SdkSupplier supplier,BaseParallelAdapter adapter){
        if (supplier!=null && adapter!=null){

            ArrayList<String> stk = supplier.succeedtk;
            if (stk!=null && !stk.isEmpty()){
                for (String tk :stk ){
                    //先尝试拼接价格，只有bidding位置且有价格返回，才会进行拼接
                    if (supplier.useBidding() && supplier.bidResultPrice > 0) {
                        tk = tk + "&bidResult=" + supplier.bidResultPrice;
                    }
                    AdvanceReport.reportReplacedCommon(tk,adapter,"成功返回");
                }
            }
        }
    }


    public static void replaceReportImp(SdkSupplier supplier,BaseParallelAdapter adapter){
        if (supplier!=null && adapter!=null){

            ArrayList<String> stk = supplier.imptk;
            if (stk!=null && !stk.isEmpty()){
                for (String tk :stk ){
                    //先尝试拼接价格，只有bidding位置且有价格返回，才会进行拼接
                    if (supplier.useBidding() && supplier.bidResultPrice > 0) {
                        tk = tk + "&bidResult=" + supplier.bidResultPrice;
                    }
                    AdvanceReport.reportReplacedCommon(tk,adapter,"展示");
                }
            }
        }
    }



    public static void replaceReportWin(SdkSupplier supplier,BaseParallelAdapter adapter){
        if (supplier!=null && adapter!=null){

            ArrayList<String> stk = supplier.wintk;
            if (stk!=null && !stk.isEmpty()){
                for (String tk :stk ){
                    //先尝试拼接价格，只有bidding位置且有价格返回，才会进行拼接
                    if (supplier.useBidding() && supplier.bidResultPrice > 0) {
                        tk = tk + "&bidResult=" + supplier.bidResultPrice;
                    }
                    AdvanceReport.reportReplacedCommon(tk,adapter,"竞胜");
                }
            }
        }
    }



    /**
     * 替换链接中部分参数的值
     *
     * @param url   原始链接
     * @param key   要替换参数的key
     * @param value 值
     * @return 替换后的链接
     */
    public static String replaceParameter(String url, String key, String value) {
        try {
            LogUtil.devDebug("replaceParameter start : key = " + key + " , value = " + value + " , url = " + url);
            if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(key)) {
                url = url.replaceAll("(" + key + "=[^&]*)", key + "=" + value);
                LogUtil.devDebug("replaceParameter finish : url = " + url);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return url;

    }
}
