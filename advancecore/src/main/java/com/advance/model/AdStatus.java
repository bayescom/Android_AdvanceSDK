package com.advance.model;

//广告得执行状态
public enum AdStatus {
    DEFAULT(1),
    START(3),
    SUCCESS(9),
    SUCCESS_WITH_GM(10),
    SHOW(12),
    FAILED(98),
    FAILED_WITH_GM(99);


    int status;

    AdStatus(int status) {
        this.status = status;
    }

}
