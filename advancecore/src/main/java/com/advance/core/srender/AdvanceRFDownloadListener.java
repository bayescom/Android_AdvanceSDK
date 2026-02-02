package com.advance.core.srender;

//下载回调，用来外部接收下载进度，当前仅穿山甲\快手 支持
public interface AdvanceRFDownloadListener {
    void onIdle(AdvanceRFADData data);

    void onDownloadStatusUpdate(AdvanceRFADData data, AdvanceRFDownloadInf downloadInf);

    void onInstalled(AdvanceRFADData data, String appName);

    class AdvanceRFDownloadInf {
        public AdvanceRFDownloadInf() {

        }

        public AdvanceRFDownloadInf(int downloadStatus, long totalBytes, long currBytes, String fileName, String appName) {
            this.downloadStatus = downloadStatus;
            this.totalBytes = totalBytes;
            this.currBytes = currBytes;
            this.fileName = fileName;
            this.appName = appName;
        }

        public int downloadStatus = AdvanceRFConstant.AD_DOWNLOAD_STATUS_ACTIVE;
        public long totalBytes;
        public long currBytes;
        public int downloadPercent;
        public String fileName;
        public String appName;

        //百分值进度
        public float getDownloadPercent() {
            if (downloadPercent > 0) {
                return downloadPercent;
            }
            if (totalBytes > 0) {
                return currBytes * 100f / totalBytes;
            }
            return 0f;
        }
    }
}
