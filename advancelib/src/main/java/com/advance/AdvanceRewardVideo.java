package com.advance;

import android.app.Activity;
import android.text.TextUtils;

import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.ServerRewardModel;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.itf.RewardGMCallBack;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceLoader;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.net.BYNetRequest;
import com.bayes.sdk.basic.net.BYReqCallBack;
import com.bayes.sdk.basic.net.BYReqModel;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;

import org.json.JSONObject;

public class AdvanceRewardVideo extends AdvanceBaseAdspot implements RewardVideoSetting {
    private AdvanceRewardVideoListener listener;
    private int orientation = ORIENTATION_VERTICAL;
    private int csjImageAcceptedSizeWidth = 1080;
    private int csjImageAcceptedSizeHeight = 1920;
    private int csjExpressWidth = 500;
    private int csjExpressHeight = 500;
    private boolean isCsjExpress = true;
    public static final String TAG = "[AdvanceRewardVideo] ";

    private String userId = "";//全渠道统一的用户id。用来服务端校验透传
    private String extraInfo = "";//全渠道统一的自定义参数。用来服务端校验透传
    private String rewardName = "";
    private int rewardCount = 0;

    @Deprecated
    private boolean isGdtVO = false;
    private boolean isMute = true;

    public static final int ORIENTATION_VERTICAL = 1;
    public static final int ORIENTATION_HORIZONTAL = 2;
    private String paraCachedSupId = ""; //并行时当前缓存成功的渠道id

    private RewardGMCallBack rewardGMCallBack;

    private Activity showActivity;


    public AdvanceRewardVideo(Activity activity, String adspotId) {
        super(activity, "", adspotId);
        isReard = true;
    }

    public AdvanceRewardVideo(String adspotId) {
        super(adspotId);
        isReard = true;
    }

    public void show(Activity activity) {
        this.showActivity = activity;
        updateADActivity(showActivity);
        show();
    }

    //避免调用出错，需要额外进行检测并打印日志提示开发者，使用正确的调用广告
    @Override
    public void show() {
        try {
            if (getADActivity() != null) {
                showActivity = getADActivity();
            }
            super.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid() {
        try {
            if (TextUtils.isEmpty(currentSDKId)) {
                LogUtil.e(TAG + "未选中任何SDK");
                return false;
            }
            if (supplierAdapters == null || supplierAdapters.size() == 0) {
                LogUtil.e(TAG + "无可用渠道");
                return false;
            }
            if (currentSdkSupplier == null) {
                LogUtil.e(TAG + "未找到当前执行渠道");
                return false;
            }
            String priority = currentSdkSupplier.priority + "";
            final BaseParallelAdapter adapter = supplierAdapters.get(priority);
            if (adapter == null) {
                LogUtil.e(TAG + "未找到当前渠道下adapter，渠道id：" + currentSDKId + ", priority = " + priority);
                return false;
            }
            //转换为
            if (adapter instanceof AdvanceRewardCustomAdapter) {
                AdvanceRewardCustomAdapter rewardCustomAdapter = (AdvanceRewardCustomAdapter) adapter;
                return rewardCustomAdapter.isValid();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }


    //设置gm监听回调
    public void setRewardGMCallBack(RewardGMCallBack rewardGMCallBack) {
        this.rewardGMCallBack = rewardGMCallBack;
        setBaseGMCall(rewardGMCallBack);
    }

    public void setCsjImageAcceptedSize(int width, int height) {
        this.csjImageAcceptedSizeWidth = width;
        this.csjImageAcceptedSizeHeight = height;

    }


    public void setCsjExpressSize(int width, int height) {
        this.csjExpressWidth = width;
        this.csjExpressHeight = height;
        isCsjExpress = true;
    }


    public void setOrientation(int orientation) {
        this.orientation = orientation;

    }

    @Override
    public Activity getShowActivity() {
        return showActivity;
    }

    @Override
    public boolean isCsjExpress() {
        return isCsjExpress;
    }

    @Deprecated
    public void setGdtVolumeOn(boolean vo) {
        isGdtVO = vo;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    @Override
    public boolean isGdtVolumeOn() {
        return isGdtVO;
    }

    @Override
    public boolean isMute() {
        return isMute;
    }


    public int getOrientation() {
        return orientation;
    }


    public int getCsjImageAcceptedSizeWidth() {
        return csjImageAcceptedSizeWidth;
    }

    public int getCsjImageAcceptedSizeHeight() {
        return csjImageAcceptedSizeHeight;
    }

    @Override
    public int getCsjExpressHeight() {
        return csjExpressHeight;
    }

    @Override
    public int getCsjExpressWidth() {
        return csjExpressWidth;
    }

    public void setAdListener(AdvanceRewardVideoListener listener) {
        advanceSelectListener = listener;
        this.listener = listener;
    }

    public String getParaCachedSupId() {
        return paraCachedSupId;
    }

    @Override
    public void paraEvent(int type, AdvanceError advanceError, SdkSupplier sdkSupplier) {
        LogUtil.max("[AdvanceRewardVideo] paraEvent: type = " + type);

        switch (type) {
            case AdvanceConstant.EVENT_TYPE_CACHED:
                if (canNextStep(sdkSupplier)) {
                    if (advanceError != null) {
                        paraCachedSupId = advanceError.msg;
                    }
                    adapterVideoCached();
                }
                break;
            case AdvanceConstant.EVENT_TYPE_ORDER:
            case AdvanceConstant.EVENT_TYPE_ERROR:
            case AdvanceConstant.EVENT_TYPE_SUCCEED:
                super.paraEvent(type, advanceError, sdkSupplier);
                break;
        }
    }

    @Override
    public void initSdkSupplier() {
        try {
            //配置渠道信息
            initSupplierAdapterList();

            initAdapter(AdvanceConfig.SDK_ID_CSJ, "csj.CsjRewardVideoAdapter");
            initAdapter(AdvanceConfig.SDK_ID_GDT, "gdt.GdtRewardVideoAdapter");
            initAdapter(AdvanceConfig.SDK_ID_MERCURY, "mry.MercuryRewardVideoAdapter");
            initAdapter(AdvanceConfig.SDK_ID_BAIDU, "baidu.BDRewardAdapter");
            initAdapter(AdvanceConfig.SDK_ID_KS, "ks.KSRewardAdapter");
            initAdapter(AdvanceConfig.SDK_ID_TANX, "tanx.TanxRewardAdapter");
            initAdapter(AdvanceConfig.SDK_ID_TAP, "tap.TapRewardAdapter");
            initAdapter(AdvanceConfig.SDK_ID_SIG, "sigmob.SigmobRewardAdapter");
            initAdapter(AdvanceConfig.SDK_ID_OPPO, "oppo.OppoRewardAdapter");
            initAdapter(AdvanceConfig.SDK_ID_HW, "huawei.HWRewardAdapter");
            initAdapter(AdvanceConfig.SDK_ID_XIAOMI, "mi.XMRewardAdapter");

            initAdapter(AdvanceConfig.SDK_ID_HONOR, "honor.HonorRewardAdapter");
            initAdapter(AdvanceConfig.SDK_ID_VIVO, "vv.VivoRewardAdapter");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void initAdapterData(SdkSupplier sdkSupplier, String clzName) {
        try {
            supplierAdapters.put(sdkSupplier.priority + "", AdvanceLoader.getRewardAdapter(clzName, getADActivity(), this));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Override
    public void selectSdkSupplierFailed() {
        onAdvanceError(listener, AdvanceError.parseErr(AdvanceError.ERROR_SUPPLIER_SELECT_FAILED));
    }


    public void adapterDidShow(SdkSupplier supplier) {
        reportAdShow(supplier);
        if (null != listener) {
            listener.onAdShow();
        }
        if (rewardGMCallBack != null) {
            rewardGMCallBack.onAdShow();
        }
    }


    public void adapterDidClicked(SdkSupplier supplier) {
        reportAdClicked(supplier);
        if (null != listener) {
            listener.onAdClicked();
        }
        if (rewardGMCallBack != null) {
            rewardGMCallBack.onAdClick();
        }
    }

    public void adapterAdDidLoaded(final AdvanceRewardVideoItem advanceRewardVideoItem, SdkSupplier supplier) {
        reportAdSucceed(supplier);
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdLoaded(advanceRewardVideoItem);
                }
                if (rewardGMCallBack != null) {
                    rewardGMCallBack.onAdSuccess();
                }
            }
        });

    }

    public void adapterVideoCached() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onVideoCached();
                }
                if (rewardGMCallBack != null) {
                    rewardGMCallBack.onVideoCached();
                }
            }
        });

    }

    public void adapterVideoSkipped() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onVideoSkip();
                }
                if (rewardGMCallBack != null) {
                    rewardGMCallBack.onVideoSkip();
                }
            }
        });

    }

    public void adapterVideoComplete() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onVideoComplete();
                }
                if (rewardGMCallBack != null) {
                    rewardGMCallBack.onVideoComplete();
                }
            }
        });

    }

    public void adapterAdClose() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdClose();
                }
                if (rewardGMCallBack != null) {
                    rewardGMCallBack.onAdClose();
                }
            }
        });

    }

    public void adapterAdReward() {
        try {
            LogUtil.simple(TAG + " 收到adn激励回调");

            if (isAdvanceServerReward()) {
                //请求服务器链接，获取激励验证结果。仅当验证通过才回调onAdReward，失败返回时通过onRewardServerInf回调出去
                LogUtil.simple(TAG + "  准备发起Advance服务端激励验证");
                final RewardServerCallBackInf callBackInf = new RewardServerCallBackInf();
                ServerRewardModel serverReward = mElevenModel.serverReward;
                if (serverReward != null) {
                    BYReqModel reqModel = new BYReqModel();
                    reqModel.reqUrl = serverReward.url;
                    reqModel.timeoutMs = 5000;
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.putOpt("timestamp", System.currentTimeMillis());
                        jsonObject.putOpt("user_id", userId);
                        jsonObject.putOpt("reward_amount", getRewardCount());
                        jsonObject.putOpt("reward_name", getRewardName());
                        jsonObject.putOpt("extra", extraInfo);

                        jsonObject.putOpt("trans_id", getAdvanceId());
                        jsonObject.putOpt("placement_id", advanceAdspotId);

                        SdkSupplier currentSupplier = getCurrentSupplier();
                        if (currentSupplier != null) {
                            jsonObject.putOpt("adn_channel_id", currentSupplier.id);
                            jsonObject.putOpt("adn_adspot_id", currentSupplier.adspotid);
                            callBackInf.supId = currentSupplier.id;
                        }

                        callBackInf.rewardAmount = getRewardCount();
                        callBackInf.rewardName = getRewardName();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    reqModel.reqBody = jsonObject;
                    BYNetRequest.post(reqModel, new BYReqCallBack() {
                        @Override
                        public void onSuccess(String ctx) {
                            try {
                                JSONObject result = new JSONObject(ctx);
                                int code = result.optInt("code");
                                String msg = result.optString("msg");

                                if (code == 1) {
                                    callBackInf.rewardVerify = true;
                                } else {
                                    callBackInf.rewardVerify = false;
                                    callBackInf.errorCode = code;
                                    callBackInf.errMsg = msg;
                                }
                                //回调开发者
                                BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                                    @Override
                                    public void call() {
                                        if (null != listener) {
                                            listener.onRewardServerInf(callBackInf);
                                        }
                                    }
                                });

                            } catch (Throwable e) {
                                e.printStackTrace();
                                callBackInf.rewardVerify = false;
                                callBackInf.errorCode = AdvanceError.ERROR_REWARD_SERVER_VERIFY_JSON_DECODE_FAILED;
                                callBackInf.errMsg = "服务端验证激励奖励，json结果解析失败";
                                //回调开发者
                                BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                                    @Override
                                    public void call() {
                                        if (null != listener) {
                                            listener.onRewardServerInf(callBackInf);
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailed(int code, String reason) {
                            callBackInf.rewardVerify = false;
                            callBackInf.errorCode = code;
                            callBackInf.errMsg = "激励奖励请求服务端验证失败，reason：" + reason;
                            //回调开发者
                            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                                @Override
                                public void call() {
                                    if (null != listener) {
                                        listener.onRewardServerInf(callBackInf);
                                    }
                                }
                            });
                        }
                    });

                } else {
                    callBackInf.errorCode = AdvanceError.ERROR_REWARD_SERVER_VERIFY_EMPTY_SDK;
                    callBackInf.errMsg = "未获取到当前广告数据，无法执行服务端验证流程";
                    BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                        @Override
                        public void call() {
                            if (null != listener) {
                                listener.onRewardServerInf(callBackInf);
                            }
                        }
                    });
                }
            } else {
                BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                    @Override
                    public void call() {
                        if (null != listener) {
                            listener.onAdReward();
                        }
                        if (rewardGMCallBack != null) {
                            rewardGMCallBack.onAdReward();
                        }
                    }
                });

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postRewardServerInf(final RewardServerCallBackInf inf) {
        if (isAdvanceServerReward()) {
            return;
        }
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onRewardServerInf(inf);
                }
            }
        });

    }

    //是否为聚合SDK端进行的服务器激励验证
    private boolean isAdvanceServerReward() {
        boolean isServerReward = false;
        try {
            if (mElevenModel != null && mElevenModel.serverReward != null) {
                isServerReward = BYStringUtil.isNotEmpty(mElevenModel.serverReward.url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isServerReward;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public void setRewardName(String name) {
        rewardName = name;
    }

    public void setRewardCount(int count) {
        rewardCount = count;
    }

    //    优先取用户配置的有效值
    public String getRewardName() {
        try {
            if (BYStringUtil.isNotEmpty(rewardName)) {
                return rewardName;
            } else {
                return mElevenModel.serverReward.rewardName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //    优先取用户配置的有效值
    public int getRewardCount() {
        try {
            if (rewardCount > 0) {
                return rewardCount;
            } else {
                return mElevenModel.serverReward.rewardCount;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
