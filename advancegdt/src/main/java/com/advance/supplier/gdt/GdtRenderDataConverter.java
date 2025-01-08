package com.advance.supplier.gdt;

import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.net.BYNetRequest;
import com.bayes.sdk.basic.net.BYReqCallBack;
import com.bayes.sdk.basic.net.BYReqModel;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.qq.e.ads.nativ.NativeUnifiedADData;
import com.qq.e.comm.constants.AdPatternType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GdtRenderDataConverter implements AdvanceRFADData {
    NativeUnifiedADData mGdtData;
    SdkSupplier mSdkSupplier;
    public static final String TAG = "[GdtRenderDataConverter] ";

    GdtDownloadElement mGdtEle;

    public GdtRenderDataConverter(NativeUnifiedADData gdtData, SdkSupplier sdkSupplier) {
        try {
            this.mGdtData = gdtData;
            this.mSdkSupplier = sdkSupplier;

            LogUtil.devDebug(TAG + "mGdtData  inf  print start ");
            LogUtil.devDebug(TAG + getTitle());
            LogUtil.devDebug(TAG + getDesc());
            LogUtil.devDebug(TAG + getVideoImageUrl());
            LogUtil.devDebug(TAG + "isVideo = " + isVideo());
            LogUtil.devDebug(TAG + "isDownloadAD = " + isDownloadAD());
            LogUtil.devDebug(TAG + "getECPM = " + getECPM());
            LogUtil.devDebug(TAG + "mGdtData  inf  print end");

//            准备解析六要素中的权限信息。
            mGdtEle = new GdtDownloadElement();
            mGdtEle.startGetPlist();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public AdvanceSdkSupplier getSdkSupplier() {
        AdvanceSdkSupplier advanceSdkSupplier = new AdvanceSdkSupplier();
        try {
            if (mSdkSupplier != null) {
                advanceSdkSupplier.adnId = mSdkSupplier.id;
                advanceSdkSupplier.adspotId = mSdkSupplier.adspotid;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return advanceSdkSupplier;
    }

    @Override
    public String getTitle() {
        try {
            if (mGdtData != null) {
                return mGdtData.getTitle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getDesc() {
        try {
            if (mGdtData != null) {
                return mGdtData.getDesc();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getSourceText() {
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            if (mGdtData != null) {
                return mGdtData.getIconUrl();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
        return "";
    }


    @Override
    public List<String> getImgList() {
        List<String> result = new ArrayList<>();
        try {
            //添加素材url
            if (mGdtData != null) {
                int patternType = mGdtData.getAdPatternType();
                if (patternType == AdPatternType.NATIVE_2IMAGE_2TEXT || patternType == AdPatternType.NATIVE_1IMAGE_2TEXT) {
                    result.add(mGdtData.getImgUrl());
                } else if (patternType == AdPatternType.NATIVE_3IMAGE) {
                    List<String> cList = mGdtData.getImgList();
                    if (cList.size() > 0) {
                        for (String imageUrl : cList) {
                            if (!BYStringUtil.isEmpty(imageUrl)) {
                                result.add(imageUrl);
                            }
                        }
                    }
                }


            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean isDownloadAD() {
        try {
            if (mGdtData != null) {
                return mGdtData.isAppAd();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            if (mGdtData != null) {
                int mode = mGdtData.getAdPatternType();
                return mode == AdPatternType.NATIVE_VIDEO;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getECPM() {
        try {
            if (mGdtData != null) {
                return mGdtData.getECPM();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        if (mGdtData == null || mGdtData.getAppMiitInfo() == null) {
            return null;
        }
        return mGdtEle;
    }

    //下载六要素
    public class GdtDownloadElement implements AdvanceRFDownloadElement {

        private final ArrayList<AdvDownloadPermissionModel> pList = new ArrayList<>();

        private BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> pListResult;

        private boolean pListGetting = false;

        //开始权限列表信息获取
        private void startGetPlist() {
            try {
                LogUtil.simple(TAG + "startGetPlist");
                if (mGdtData.getAppMiitInfo() != null) {
                    pListGetting = true;
                    //请求url，获取权限object信息
                    BYReqModel reqModel = new BYReqModel();
                    reqModel.reqUrl = mGdtData.getAppMiitInfo().getPermissionsUrl() + "&resType=api";
                    BYNetRequest.get(reqModel, new BYReqCallBack() {
                        @Override
                        public void onSuccess(String result) {
                            try {
                                pListGetting = false;
                                LogUtil.simple(TAG + " GdtDownloadElement plist result success");
                                JSONObject jsonObject = new JSONObject(result);
                                Iterator<String> iterator = jsonObject.keys();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    try {
                                        Object value = jsonObject.get(key);
                                        LogUtil.simple(TAG + "Key: " + key + ", Value: " + value.toString());

                                        if (value instanceof JSONObject) {
                                            // 如果该键值对应的值是 JSONObject，则递归遍历该 JSONObject
                                            JSONObject pModel = jsonObject.optJSONObject(key);
                                            if (pModel != null) {
                                                //解析权限信息
                                                String pTitle = pModel.optString("title");
                                                String pDesc = pModel.optString("desc");
                                                if (!BYStringUtil.isEmpty(pTitle)) {
                                                    AdvDownloadPermissionModel advDownloadPermissionModel = new AdvDownloadPermissionModel();
                                                    advDownloadPermissionModel.permTitle = pTitle;
                                                    advDownloadPermissionModel.permDesc = pDesc;
                                                    pList.add(advDownloadPermissionModel);
                                                }
                                            }
                                        } else {
                                            // 否则，直接打印该键值对应的值
                                        }
                                    } catch (JSONException e) {
                                        // 异常处理
                                        e.printStackTrace();
                                    }
                                }
                                BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                                    @Override
                                    public void call() {
                                        if (pListResult != null) {
                                            pListResult.invoke(pList);
                                        }
                                    }
                                });

                                LogUtil.simple(TAG + "pList size = " + pList.size());
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(int i, String s) {
                            pListGetting = false;

                            LogUtil.e(TAG + " GdtDownloadElement plist result err");
                            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                                @Override
                                public void call() {
                                    if (pListResult != null) {
                                        pListResult.invoke(pList);
                                    }
                                }
                            });
                        }
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }

        @Override
        public String getAppName() {
            try {
                return mGdtData.getAppMiitInfo().getAppName();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppVersion() {
            try {
                return mGdtData.getAppMiitInfo().getVersionName();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppDeveloper() {
            try {
                return mGdtData.getAppMiitInfo().getAuthorName();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPrivacyUrl() {
            try {
                return mGdtData.getAppMiitInfo().getPrivacyAgreement();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPermissionUrl() {
//            try {
//                return mGdtData.getAppMiitInfo().getPermissionsUrl() + "&resType=api";
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
            return "";
        }


        @Override
        public String getFunctionDescUrl() {
            try {
                return mGdtData.getAppMiitInfo().getDescriptionUrl();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getFunctionDescText() {
            return "";
        }

        @Override
        public void getPermissionList(BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> pListResult) {
            try {
                this.pListResult = pListResult;

                //如果已经拿到结果了，且结果不为空，此时可以立即回调出去
                if (!pListGetting && pList.size() > 0 && pListResult != null) {
                    pListResult.invoke(pList);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public long getPkgSize() {
//            返回大小信息
            try {
                return mGdtData.getAppMiitInfo().getPackageSizeBytes();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return 0;
        }


    }


}
