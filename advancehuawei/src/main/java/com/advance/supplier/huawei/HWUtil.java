package com.advance.supplier.huawei;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import com.advance.AdvanceConfig;
import com.advance.AdvanceConstant;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.net.BYNetRequest;
import com.bayes.sdk.basic.net.BYReqCallBack;
import com.bayes.sdk.basic.net.BYReqModel;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.huawei.hms.ads.BiddingInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class HWUtil {
    public static final String TAG = "[HWUtil] ";

    public static void notifyBid(BiddingInfo biddingInfo, boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        try {
            if (biddingInfo != null) {
                BYReqModel reqModel = new BYReqModel();
                boolean report = false;
                if (isWin) {
                    String win = biddingInfo.getNurl();
                    if (BYStringUtil.isNotEmpty(win)) {
                        report = true;

                        reqModel.reqUrl = win;
                        LogUtil.simple(TAG + "notifyBid win start ");
//若鲸鸿动能广告竞胜，调用getNurl() 竞价成功通知URL，URL中含有宏替换 SECOND_PRICE、AUCTION_CURRENCY，通知SDK竞胜结果；
                    }

                } else {
                    String loss = biddingInfo.getLurl();

                    if (BYStringUtil.isNotEmpty(loss)) {
                        report = true;

                        LogUtil.simple(TAG + "notifyBid loss start ");

                        //进行宏替换
//若广告竞败，调用getLurl() 获取竞价失败通知其他成功的URL，URL中含有宏替换（具体宏：AUCTION_LOSS、AUCTION_PRICE、AUCTION_CURRENCY、AUCTION_APP_PKG、AUCTION_APP_NAME、AUCTION_CP_ID）传入竞败原因，
                        loss = loss.replace("AUCTION_LOSS", "102");
                        loss = loss.replace("AUCTION_PRICE", "" + winPrice);
                        loss = loss.replace("AUCTION_CURRENCY", "CNY");

                        String winID = "";
                        if (referBidInfo != null) {
                            winID = (String) referBidInfo.get(AdvanceConstant.BID_RESULT_KEY_WIN_SDK_ID);
                        }
                        String cpID = getString(winID);
                        loss = loss.replace("AUCTION_CP_ID", cpID);

                        reqModel.reqUrl = loss;
                    }
                }

                if (report)
                    BYNetRequest.get(reqModel, new BYReqCallBack() {
                        @Override
                        public void onSuccess(String s) {
                            LogUtil.simple(TAG + "notifyBid success");

                        }

                        @Override
                        public void onFailed(int i, String s) {
                            LogUtil.simple(TAG + "notifyBid failed");

                        }
                    });


            }
        } catch (Exception e) {

        }
    }

    private static String getString(String winID) {
        String cpID = "100";
        if (BYStringUtil.isNotEmpty(winID)) {
            switch (winID) {
                case AdvanceConfig.SDK_ID_GDT:
                    cpID = "1";
                    break;
                case AdvanceConfig.SDK_ID_CSJ:
                    cpID = "2";
                    break;
                case AdvanceConfig.SDK_ID_BAIDU:
                    cpID = "3";
                    break;
                case AdvanceConfig.SDK_ID_KS:
                    cpID = "4";
                    break;
                case AdvanceConfig.SDK_ID_TANX:
                    cpID = "6";
                    break;
                case AdvanceConfig.SDK_ID_VIVO:
                    cpID = "7";
                    break;
                case AdvanceConfig.SDK_ID_OPPO:
                    cpID = "8";
                    break;
                case AdvanceConfig.SDK_ID_XIAOMI:
                    cpID = "9";
                    break;
            }
        }
        return cpID;
    }

//    public static synchronized void initAD(BaseParallelAdapter adapter) {
//        try {
//            final String tag = "[HWUtil.initAD] ";
//            String eMsg;
//            if (adapter == null) {
//                eMsg = tag + "initAD failed BaseParallelAdapter null";
//                LogUtil.e(eMsg);
//                return;
//            }
//
//            SdkSupplier supplier = adapter.sdkSupplier;
//            if (supplier == null) {
//                eMsg = tag + "initAD failed BaseParallelAdapter null";
//
//                LogUtil.e(eMsg);
//                return;
//            }
//
//            boolean hasInit = AdvanceHWManager.getInstance().hasInit;
//            if (hasInit) {
//                LogUtil.simple(tag + " already init");
//                return;
//            }
//            Context context = adapter.getRealContext();
//
//            HwAds.init(context.getApplicationContext());
//        } catch (Exception e) {
//
//        }
//
//    }

    public static synchronized double getPrice(BiddingInfo biddingInfo) {
        double result = 0;
        try {
            if (biddingInfo != null) {
                //接口获取本条广告的eCPM出价（元/千次展示）；
                result = biddingInfo.getPrice() * 1000;
            }
        } catch (Exception e) {
        }
        return result;
    }


    //获取素材链接，可能是本地路径、或者https地址
    public static String getMaterialPath(Context context, Uri uri) {
        try {
            if (uri == null) return null;

            // 检查Uri的scheme
            String scheme = uri.getScheme();
            if (scheme == null) {
                return uri.getPath(); // 可能直接是路径字符串
            }

            switch (scheme) {
                case "file":
                    return uri.getPath(); // 直接返回路径
                case "content":
                    // 先尝试查询获取路径（可能对媒体文件有效）
                    String pathFromQuery = getFilePathFromContentUri(context, uri);
                    if (pathFromQuery != null) {
                        return pathFromQuery;
                    }
                    // 查询失败，尝试DocumentsContract处理
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if (DocumentsContract.isDocumentUri(context, uri)) {
                            String docPath = getFilePathFromDocumentUri(context, uri);
                            if (docPath != null) {
                                return docPath;
                            }
                        }
                    }
                    // 最后的手段：复制到临时文件
                    try {
                        return copyFileFromContentUriToTempFile(context, uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                default:
                    return uri.toString(); // 不支持的其他scheme
            }
        } catch (Exception e) {
            return null;
        }
    }
// 这里需要嵌入上面定义的 getFilePathFromContentUri, getFilePathFromDocumentUri, copyFileFromContentUriToTempFile 等方法

    public static String copyFileFromContentUriToTempFile(Context context, Uri contentUri) throws IOException {
        String fileName = getFileNameFromUri(context, contentUri);
        if (fileName == null) {
            fileName = "temp_file_" + System.currentTimeMillis();
        }
        File tempFile = new File(context.getCacheDir(), fileName); // 使用缓存目录
        try (InputStream inputStream = context.getContentResolver().openInputStream(contentUri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            if (inputStream == null) return null;
            byte[] buffer = new byte[4 * 1024]; // 4KB buffer
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
        return tempFile.getAbsolutePath(); // 返回临时文件的路径
    }

    private static String getFileNameFromUri(Context context, Uri uri) {
        String fileName = null;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }


    public static String getFilePathFromDocumentUri(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.MediaColumns.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String getFilePathFromContentUri(Context context, Uri contentUri) {
        String filePath = null;
        String[] projection = {MediaStore.MediaColumns.DATA}; // 或者使用 MediaStore.MediaColumns.DATA
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                filePath = cursor.getString(columnIndex);
                // 即使查询到，某些情况下filePath可能为空，需要备用方案
                if (filePath != null) {
                    return filePath;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // 如果上述方法失败，尝试下一节的其他方法
        return filePath;
    }
}
