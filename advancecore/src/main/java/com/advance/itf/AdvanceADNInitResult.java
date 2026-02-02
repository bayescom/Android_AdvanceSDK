package com.advance.itf;

public interface AdvanceADNInitResult {
    void success();

    void fail(String code, String msg);
}