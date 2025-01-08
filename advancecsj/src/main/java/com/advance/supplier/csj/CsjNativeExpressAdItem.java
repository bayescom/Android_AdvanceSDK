package com.advance.supplier.csj;

import android.app.Activity;
import android.view.View;

import com.advance.AdvanceConfig;
import com.advance.AdvanceNativeExpressAdItem;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTDislikeDialogAbstract;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;

@Deprecated
public class CsjNativeExpressAdItem implements AdvanceNativeExpressAdItem {
    private CsjNativeExpressAdapter csjNativeExpressAdapter;
    private TTNativeExpressAd ttNativeExpressAd;
    private TTNativeExpressAd.ExpressAdInteractionListener expressAdInteractionListener;
    private TTAdDislike.DislikeInteractionCallback dislikeListener;
    private Activity activity;

    @Override
    public String getSdkTag() {
        return AdvanceConfig.SDK_TAG_CSJ;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_CSJ;
    }

    public CsjNativeExpressAdItem(Activity activity, CsjNativeExpressAdapter csjNativeExpressAdapter, TTNativeExpressAd ttNativeExpressAd) {
        this.activity = activity;
        this.csjNativeExpressAdapter = csjNativeExpressAdapter;
        this.ttNativeExpressAd = ttNativeExpressAd;
    }

    public View getExpressAdView() {
        try {
            return ttNativeExpressAd.getExpressAdView();
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getImageMode() {
        try {

            return ttNativeExpressAd.getImageMode();
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Deprecated
    public List<FilterWord> getFilterWords() {
        try {

//            return ttNativeExpressAd.getFilterWords();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    public void setExpressInteractionListener(final TTNativeExpressAd.ExpressAdInteractionListener listener) {
        this.expressAdInteractionListener = listener;
    }

    public void setDownloadListener(TTAppDownloadListener ttAppDownloadListener) {
        if (null != ttNativeExpressAd) {
            ttNativeExpressAd.setDownloadListener(ttAppDownloadListener);
        }

    }

    public int getInteractionType() {
        try {

            return ttNativeExpressAd.getInteractionType();
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void render() {
        try {
            if (null != ttNativeExpressAd) {
                ttNativeExpressAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int i) {
                        if (null != csjNativeExpressAdapter) {
                            csjNativeExpressAdapter.onAdItemClicked(view);
                        }
                        if (null != expressAdInteractionListener) {
                            expressAdInteractionListener.onAdClicked(view, i);
                        }
                    }

                    @Override
                    public void onAdShow(View view, int i) {
                        if (null != csjNativeExpressAdapter) {
                            csjNativeExpressAdapter.onAdItemShow(view);
                        }
                        if (null != expressAdInteractionListener) {
                            expressAdInteractionListener.onAdShow(view, i);
                        }

                    }

                    @Override
                    public void onRenderFail(View view, String msg, int code) {
                        if (null != csjNativeExpressAdapter) {
                            csjNativeExpressAdapter.onAdItemRenderFailed(view, msg, code);
                        }
                        if (null != expressAdInteractionListener) {
                            expressAdInteractionListener.onRenderFail(view, msg, code);
                        }

                    }

                    @Override
                    public void onRenderSuccess(View view, float v, float v1) {
                        if (null != csjNativeExpressAdapter) {
                            csjNativeExpressAdapter.onAdItemRenderSuccess(view);
                        }
                        if (null != expressAdInteractionListener) {
                            expressAdInteractionListener.onRenderSuccess(view, v, v1);
                        }

                    }
                });
                ttNativeExpressAd.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
                    @Override
                    public void onShow() {
                        if (null != dislikeListener) {
                            dislikeListener.onShow();
                        }
                    }

                    @Override
                    public void onSelected(int i, String s, boolean enforce) {
                        if (null != dislikeListener) {
                            dislikeListener.onSelected(i, s, enforce);
                        }
                        if (null != csjNativeExpressAdapter) {
                            csjNativeExpressAdapter.onAdItemClose(getExpressAdView());
                        }
                    }

                    @Override
                    public void onCancel() {
                        if (null != dislikeListener) {
                            dislikeListener.onCancel();
                        }
                    }

//                    @Override
//                    public void onRefuse() {
//                        if (null != dislikeListener) {
//                            dislikeListener.onRefuse();
//                        }
//                    }
                });

                BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                    @Override
                    public void call() {
                        ttNativeExpressAd.render();
                    }
                });
            } else {
                if (csjNativeExpressAdapter != null)
                    csjNativeExpressAdapter.onAdItemErr(AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, "ttNativeExpressAd null"));

            }
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                if (csjNativeExpressAdapter != null)
                    csjNativeExpressAdapter.onAdItemErr(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() {
        try {
            if (null != ttNativeExpressAd) {
                ttNativeExpressAd.destroy();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void setDislikeCallback(Activity activity, final TTAdDislike.DislikeInteractionCallback dislikeInteractionCallback) {
        try {
            this.dislikeListener = dislikeInteractionCallback;


            TTAdDislike.DislikeInteractionCallback dislikeInteractionCallback1 = new TTAdDislike.DislikeInteractionCallback() {
                @Override
                public void onShow() {
                    if (null != dislikeInteractionCallback) {
                        dislikeInteractionCallback.onShow();
                    }
                }

                @Override
                public void onSelected(int i, String s, boolean enforce) {
                    if (null != dislikeInteractionCallback) {
                        dislikeInteractionCallback.onSelected(i, s, enforce);
                    }
                    if (null != csjNativeExpressAdapter) {
                        csjNativeExpressAdapter.onAdItemClose(getExpressAdView());
                    }
                }

                @Override
                public void onCancel() {
                    if (null != dislikeInteractionCallback) {

                        dislikeInteractionCallback.onCancel();
                    }
                }

//                @Override
//                public void onRefuse() {
//                    if (null != dislikeInteractionCallback) {
//
//                        dislikeInteractionCallback.onRefuse();
//                    }
//                }
            };

            if (null != ttNativeExpressAd) {
                ttNativeExpressAd.setDislikeCallback(activity, dislikeInteractionCallback1);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public void setDislikeDialog(TTDislikeDialogAbstract ttDislikeDialogAbstract) {
        try {
            if (null != ttNativeExpressAd) {
                ttNativeExpressAd.setDislikeDialog(ttDislikeDialogAbstract);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
