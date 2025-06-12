package com.advance.supplier.oppo;

import android.view.View;

import com.advance.core.srender.AdvanceRFADData;
import com.heytap.msp.mobad.api.params.INativeComplianceListener;

import java.util.List;

//oppo 特殊的自渲染接口类，在示例中需要额外操作
public interface AdvanceRFADDataOppo extends AdvanceRFADData {
    void bindToComplianceView(List<View> privacyClickViews, INativeComplianceListener privacyListener, List<View> permissionClickViews, INativeComplianceListener permissionListener, List<View> appDescClickViews, final INativeComplianceListener appDescListener);
}
