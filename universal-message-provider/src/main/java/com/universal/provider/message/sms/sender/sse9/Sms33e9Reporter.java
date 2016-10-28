package com.universal.provider.message.sms.sender.sse9;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.sms.cpa.CReportReq;
import com.sms.cpa.CSendRet;
import com.sms.impl.Queue;
import com.sms.pub.MyRunnable;
import com.universal.provider.message.entity.Sms;
import com.universal.provider.message.mapper.SmsMapper;
import com.universal.provider.message.sms.CacheKey;
import com.universal.provider.message.sms.SmsReporter;
import com.universal.provider.message.sms.SmsSender;
import com.universal.provider.message.sms.SmsStatus;

public class Sms33e9Reporter extends MyRunnable implements SmsReporter {

    final static Logger logger = LoggerFactory.getLogger(Sms33e9Reporter.class);

    @Autowired
    private SmsMapper smsMapper;

    private Queue queue = null;
    private Object lock = new Object();

    private SmsSender failedHandler;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {

        while (!bStop) {

            synchronized (lock) {
                resetIdle();

                if (!queue.isEmpty()) {

                    Object received = queue.pop();

                    if (received instanceof CSendRet) {

                        CSendRet report = (CSendRet) received;

                        String reqId = report.sReqID;
                        int status = report.bStatus;
                        String message = report.sMsg;
                        String sequence = report.sSerialNo;

                        logger.info("CSendRet: reqId=" + reqId + ", status=" + status + ", message=" + message + ", sequence=" + sequence);

                        Sms sms = (Sms) redisTemplate.opsForValue().get(String.format(CacheKey.SMS, sequence));
                        String smsId = sms != null ? String.valueOf(sms.getId()) : StringUtils.EMPTY;

                        if (status != 0x01) {

                            logger.error(String.format("Report: %s sending failed [id=%s].", sequence, smsId));
                            sms.setStatus(SmsStatus.FAL);
                            this.onSubmittedFailed(sms);

                        } else {
                            logger.info(String.format("Sent %s to 33e9 successed [id=%s].", sequence, smsId));

                            redisTemplate.opsForValue().set(String.format(CacheKey.SSE9_REQUEST, reqId), sms, 1, TimeUnit.DAYS);

                            sms.setStatus(SmsStatus.SBM);
                            this.onSubmittedSuccess(sms);
                        }
                    } else if (received instanceof CReportReq) {

                        CReportReq report = (CReportReq) received;

                        String reqId = report.sReqID;
                        byte status = report.bStatus;
                        String message = report.sMsg;
                        String phone = report.sPhone;
                        String sequence = report.sSerialNo;

                        logger.info("CReportReq: reqId=" + reqId + ", status=" + status + ", message=" + message + ", phone=" + phone + ", sequence=" + sequence);

                        Sms sms = (Sms) redisTemplate.opsForValue().get(String.format(CacheKey.SSE9_REQUEST, reqId));
                        String smsId = sms != null ? String.valueOf(sms.getId()) : StringUtils.EMPTY;

                        if (status != 0x03) {
                            logger.error(String.format("Report: %s arriving failed [id=%s].", reqId, smsId));
                            sms.setStatus(SmsStatus.FAL);
                            this.onArrivedFailed(sms, reqId);
                        } else {
                            logger.info(String.format("Report: %s has arrived [id=%s].", reqId, smsId));
                            sms.setStatus(SmsStatus.ARV);
                            this.onArrivedSuccess(sms);
                        }
                    } else {

                        logger.info("MO: " + received);
                    }
                }

                try {
                    lock.wait(200);
                } catch (InterruptedException e) {
                    logger.error("Wait for thread failed.", e);
                }
            }
        }

    }

    @Override
    public boolean start() {

        boolean result = false;

        if (getIdle() > getMaxIdle() || !isAlive()) {

            logger.info("Starting 33e9 reporter ...");
            result = super.start();
            logger.info("Reporter started.");
        } else {

            result = true;
        }

        return result;
    }

    @Override
    public void stop() {

        logger.info("Stopping 33e9 reporter ...");
        synchronized (lock) {
            super.bStop = true;
            lock.notifyAll();
        }

        super.stop();

        if (thread != null) {
            try {
                thread.join(500);
            } catch (InterruptedException e) {
                logger.error("Join thread failed.", e);
            }
        }
    }

    @Override
    protected void end() {

    }

    @Override
    public void onSubmittedSuccess(final Sms sms) {

        if (sms != null) {
            smsMapper.updateSms(sms.getId(), sms.getStatus(), sms.getChannel());
            smsMapper.insertLog(sms.getId(), sms.getStatus(), sms.getChannel());
        }
    }

    @Override
    public void onArrivedSuccess(final Sms sms) {

        if (sms != null) {
            smsMapper.updateSms(sms.getId(), sms.getStatus(), sms.getChannel());
            smsMapper.insertLog(sms.getId(), sms.getStatus(), sms.getChannel());
        }
    }

    @Override
    public void onSubmittedFailed(final Sms sms) {

        if (sms != null) {

            smsMapper.updateSms(sms.getId(), sms.getStatus(), sms.getChannel());
            smsMapper.insertLog(sms.getId(), sms.getStatus(), sms.getChannel());

            if (failedHandler != null) {
                try {
                    this.failedHandler.send(sms);
                } catch (Exception e) {
                    logger.error("Send " + sms.getId() + " failed.", e);
                }
            } else {
                redisTemplate.delete(String.format(CacheKey.SMS, sms.getId()));
                logger.info(String.format("Removed %d from redis, because of no failed handler.", sms.getId()));
            }
        }
    }

    @Override
    public void onArrivedFailed(final Sms sms) {

        if (sms != null) {

            smsMapper.updateSms(sms.getId(), sms.getStatus(), sms.getChannel());
            smsMapper.insertLog(sms.getId(), sms.getStatus(), sms.getChannel());

            if (failedHandler != null) {
                try {
                    this.failedHandler.send(sms);
                } catch (Exception e) {
                    logger.error("Send " + sms.getId() + " failed.", e);
                }
            } else {
                redisTemplate.delete(String.format(CacheKey.SMS, sms.getId()));
                logger.info(String.format("Removed %d from redis, because of no failed handler.", sms.getId()));
            }
        }
    }

    public void onArrivedFailed(final Sms sms, final String seqId) {

        this.onArrivedFailed(sms);

        if (failedHandler == null) {
            redisTemplate.delete(String.format(CacheKey.SSE9_REQUEST, seqId));
            logger.info(String.format("Removed 33E9 %d from redis, because of no failed handler.", sms.getId()));
        }
    }

    @Override
    public void setFailedHander(SmsSender sender) {

        this.failedHandler = sender;
    }

    @Override
    public SmsSender getFailedHander() {

        return this.failedHandler;
    }

}
