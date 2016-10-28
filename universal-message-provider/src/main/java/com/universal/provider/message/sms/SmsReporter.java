package com.universal.provider.message.sms;

import com.universal.provider.message.entity.Sms;

public interface SmsReporter {

    boolean start();

    void stop();

    SmsSender getFailedHander();
    
    void setFailedHander(final SmsSender sender);

    void onSubmittedSuccess(final Sms sms);

    void onArrivedSuccess(final Sms sms);

    void onSubmittedFailed(final Sms sms);

    void onArrivedFailed(final Sms sms);
}
