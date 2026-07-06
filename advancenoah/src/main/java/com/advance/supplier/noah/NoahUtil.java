package com.advance.supplier.noah;

import com.advance.BaseParallelAdapter;
import com.noah.api.AdError;
import com.noah.api.BiddingLossReason;
import com.noah.remote.IBaseAdRemote;

import java.util.Map;

public class NoahUtil {
    public static void bid(IBaseAdRemote noahAD, boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        if (noahAD != null) {
            if (isWin) {
                noahAD.sendWinNotification((int) winPrice);
            } else {
                noahAD.sendLossNotification((int) winPrice, BiddingLossReason.LOW_PRICE);
            }
        }
    }

    public static void loadErr(BaseParallelAdapter adapter, AdError err) {
        try {
            if (adapter != null && err != null) {
                int errCode = err.getErrorCode();
                int subErrCode = err.getErrorSubCode();
                String code = "" + errCode;
                if (subErrCode > 0) {
                    code = code + "-" + subErrCode;
                }
                adapter.handleFailed(code, err.getErrorMessage());
            }
        } catch (Exception e) {

        }
    }
}
