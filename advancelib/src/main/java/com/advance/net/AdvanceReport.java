package com.advance.net;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.advance.AdvanceConfig;
import com.advance.AdvanceConstant;
import com.advance.AdvanceSetting;
import com.advance.model.AdvanceError;
import com.advance.model.AdvanceReportModel;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.net.BYNetRequest;
import com.bayes.sdk.basic.net.BYReqCallBack;
import com.bayes.sdk.basic.net.BYReqModel;
import com.bayes.sdk.basic.util.BYUtil;

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
        try {
            BYReqModel request = new BYReqModel();
            request.reqUrl = urlString;
            request.timeoutMs = 10000;
            //get请求 会自动重定向，上报不成功尝试重试上报
            BYNetRequest.get(request, new BYReqCallBack() {
                @Override
                public void onSuccess(String s) {
                    String devTips = "上报成功,url=" + urlString + ",msg=" + s;
                    LogUtil.devDebugAuto(devTips, "上报成功");
                }

                @Override
                public void onFailed(int i, String s) {
                    LogUtil.devDebug("上报失败, " + "code = " + i + ", msg = " + s);
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
            if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(key)) {
                url = url.replaceAll("(" + key + "=[^&]*)", key + "=" + value);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return url;

    }
}
