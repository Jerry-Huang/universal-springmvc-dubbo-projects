package com.universal.provider.message.mapper;

import org.apache.ibatis.annotations.Param;

import com.universal.provider.message.entity.Sms;

public interface SmsMapper {

    public long insertSms(final Sms sms);

    public void updateSms(@Param("smsId") long smsId, @Param("status") String status, @Param("channel") String channel);

    public int findQuantityToday(@Param("to") String to, @Param("content") String content);

    public void insertLog(@Param("smsId") long smsId, @Param("status") String status, @Param("channel") String channel);

    public int findSuccessedQuantityToday(@Param("to") String to, @Param("content") String content, @Param("channel") String channel);
}
