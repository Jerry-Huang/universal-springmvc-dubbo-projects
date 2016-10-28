package com.universal.provider.message.sms.sender.sse9;

import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sms.impl.GSTEngineFactory;
import com.sms.pub.MyRunnable;
import com.universal.provider.message.entity.Sms;
import com.universal.provider.message.mapper.SmsMapper;
import com.universal.provider.message.sms.SmsChannel;
import com.universal.provider.message.sms.SmsReporter;
import com.universal.provider.message.sms.SmsSender;
import com.universal.provider.message.sms.SmsStatus;

public class Sms33e9Sender extends MyRunnable implements SmsSender {

    @Autowired
    private SmsMapper smsMapper;

    private int interval = 10;
    private int maxSendingTimes = 3;
    private Object lock = new Object();
    private Sms33e9Reporter reporter = null;

    private volatile boolean ready = false;
    private GSTEngineFactory engine = null;
    private BlockingQueue<Sms> queue = null;

    private final static Logger logger = LoggerFactory.getLogger(Sms33e9Sender.class);

    public Sms33e9Sender() {

        queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void send(final Sms sms) throws Exception {

        sms.setChannel(SmsChannel.SSE9);

        if (smsMapper.findSuccessedQuantityToday(sms.getTo(), sms.getContent(), SmsChannel.SSE9) >= this.maxSendingTimes) {

            logger.error("The maximum of " + maxSendingTimes + " per message per day has been exceeded");

            sms.setStatus(SmsStatus.REJ);
            reporter.onSubmittedFailed(sms);
            return;
        }

        if (!ready) {
            logger.warn("33e9 is not ready.");
            sms.setStatus(SmsStatus.REJ);
            this.reporter.onSubmittedFailed(sms);
        } else if (!this.queue.offer(sms)) {

            logger.error("Put sms [" + sms + "] in queue failed.");
        }
    }

    @Override
    public boolean start() {

        logger.info("Starting 33e9 sender ...");

        if (!isUserNameOk()) {
            logger.warn("Invalid user name.");
            return false;
        }

        this.engine = GSTEngineFactory.getInstance();
        boolean result = super.start();

        logger.info("Sender started.");
        return result;
    }

    private boolean isUserNameOk() {

        ResourceBundle resourceBundle = ResourceBundle.getBundle("sms_engine");
        final String userName = resourceBundle.getString("username");
        return StringUtils.isNotBlank(userName);
    }

    @Override
    public void stop() {

        logger.info("Stopping 33e9 sender ...");
        synchronized (lock) {
            super.bStop = true;
            lock.notifyAll();
        }

        super.stop();
        reporter.stop();

        if (thread != null) {
            try {
                thread.join(interval);
            } catch (InterruptedException e) {
                logger.error("Join thread failed.", e);
            }

            engine.logoutReq();
        }
    }

    public void run() {

        while (!bStop) {

            synchronized (lock) {
                resetIdle();
                reporter.setQueue(GSTEngineFactory.getQueue());

                reporter.start();

                if (engine.getConn() == null) {
                    if (engine.loginEngin() == 0) {
                        ready = true;
                        logger.info("Connected and login sucessed.");
                    } else {
                        ready = false;
                        logger.error("Connected or login failed.");
                        engine.logoutReq();
                        try {
                            lock.wait(10000);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }
                } else if (!ready) {
                    ready = true;
                }

                Sms sms = null;
                try {
                    sms = this.queue.poll(interval * 100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.error("Poll sms from queue failed.", e);
                }

                if (sms == null) {
                    continue;
                }

                logger.info("Sending sms: " + sms);

                engine.setSequenceSMS((int) sms.getId());
                int flag = engine.sendSigleSMS(sms.getFrom(), sms.getTo(), sms.getContent());
                if (flag == 0) {
                    logger.info("Send sms " + sms.getId() + " sucessfully.");
                    sms.setStatus(SmsStatus.SBM);
                    this.reporter.onSubmittedSuccess(sms);
                } else {
                    logger.error("Send sms " + sms.getId() + " failed, flag = " + flag + " .");
                    sms.setStatus(SmsStatus.FAL);
                    this.reporter.onSubmittedFailed(sms);
                }

                try {
                    lock.wait(interval);
                } catch (InterruptedException e) {
                    logger.error("Wait for thread failed.", e);
                }

            }
        }

        logger.info("Stopped normally!");
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    protected void end() {

    }

    @Override
    public void setReporter(final SmsReporter reporter) {

        if (reporter instanceof Sms33e9Reporter) {
            this.reporter = (Sms33e9Reporter) reporter;
        } else {
            logger.error("Unsupported reporter.");
        }
    }

    public int getMaxSendingTimes() {
        return maxSendingTimes;
    }

    public void setMaxSendingTimes(int maxSendingTimes) {
        this.maxSendingTimes = maxSendingTimes;
    }
}
