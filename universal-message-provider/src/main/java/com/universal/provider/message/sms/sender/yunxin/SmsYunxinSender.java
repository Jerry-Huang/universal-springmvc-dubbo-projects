package com.universal.provider.message.sms.sender.yunxin;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.universal.exception.UnexpectedHttpStatusCodeException;
import com.universal.provider.message.entity.Sms;
import com.universal.provider.message.sms.SmsChannel;
import com.universal.provider.message.sms.SmsReporter;
import com.universal.provider.message.sms.SmsStatus;
import com.universal.provider.message.sms.sender.AbstractSmsSender;
import com.universal.utils.HttpUtils;

public class SmsYunxinSender extends AbstractSmsSender implements Runnable {

    private String url = null;
    private String account = null;
    private String password = null;
    private SmsReporter reporter = null;

    private int interval = 10;
    private Thread thread = null;
    private Object lock = new Object();
    private BlockingQueue<Sms> queue = null;
    private volatile boolean stopped = false;

    private final static Logger logger = LoggerFactory.getLogger(SmsYunxinSender.class);

    public SmsYunxinSender(final String url, final String account, final String password) {

        this.url = url;
        this.account = account;
        this.password = password;

        queue = new LinkedBlockingQueue<>();
    }

    @Override
    public boolean start() {

        logger.info("Starting Yunxin sender ...");

        thread = new Thread(this);
        this.stopped = false;
        thread.start();

        logger.info("Sender started.");
        return true;
    }

    @Override
    public void stop() {

        logger.info("Stopping Yunxin sender ...");
        synchronized (lock) {
            this.stopped = true;
            lock.notifyAll();
        }

        reporter.stop();

        if (thread != null) {
            try {
                thread.join(interval);
            } catch (InterruptedException e) {
                logger.error("Join thread failed.", e);
            }
        }
    }

    @Override
    public void send(Sms sms) throws Exception {

        sms.setChannel(SmsChannel.YUNXIN);

        if (findSuccessedQuantityToday(sms, SmsChannel.YUNXIN) >= this.maxSendingTimes) {

            logger.error("The maximum of " + maxSendingTimes + " per message per day has been exceeded");

            sms.setStatus(SmsStatus.REJ);
            reporter.onSubmittedFailed(sms);
            return;
        }

        if (!this.queue.offer(sms)) {

            logger.error("Put sms [" + sms + "] in queue failed.");
        }

    }

    private static boolean isSuccess(final String response) {

        return response.matches(".*sms&stat=100&message=.+");
    }

    @Override
    public void run() {
        while (!stopped) {

            synchronized (lock) {
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

                try {

                    String response = HttpUtils.get(String.format("%s?uid=%s&pwd=%s&mobile=%s&encode=utf8&content=%s", url, account, password, sms.getTo(),
                            URLEncoder.encode(sms.getContent() + "【农迈天下】", "UTF-8")));

                    if (isSuccess(response)) {

                        logger.info(String.format("Send sms %s sucessfully.", sms));

                        sms.setStatus(SmsStatus.SNT);
                        this.reporter.onSubmittedSuccess(sms);
                    } else {

                        logger.error(String.format("Send sms %s failed.", sms));

                        sms.setStatus(SmsStatus.FAL);
                        this.reporter.onSubmittedFailed(sms);
                    }
                } catch (IOException | UnexpectedHttpStatusCodeException e) {

                    logger.error(String.format("Send sms %s failed.", sms), e);

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

    @Override
    public void setReporter(SmsReporter reporter) {

        this.reporter = reporter;
    }
}
