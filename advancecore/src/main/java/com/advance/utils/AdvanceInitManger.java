package com.advance.utils;


import com.bayes.sdk.basic.itf.BYBaseCallBack;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdvanceInitManger {
    private static AdvanceInitManger instance;

    public static synchronized AdvanceInitManger getInstance() {
        if (instance == null) {
            instance = new AdvanceInitManger();
        }
        return instance;
    }

    private final AtomicBoolean isInitializing = new AtomicBoolean(false);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final CountDownLatch initLatch = new CountDownLatch(1);


    //线程安全的初始化处理逻辑
    public void initialize(BYBaseCallBack initCall) {
        try {
//            if (isInitialized.get()) {
//                return;
//            }

            if (isInitializing.compareAndSet(false, true)) {
                // 当前线程获得初始化权限
                try {
                    if (initCall != null) {
                        initCall.call();
                    }
//                    isInitialized.set(true);
                } finally {
                    initLatch.countDown();
                }
            } else {
                // 等待其他线程完成初始化
                try {
                    initLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
