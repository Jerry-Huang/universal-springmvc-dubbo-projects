package com.universal.provider.message.sms.sender.chuanglan;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.universal.exception.UnexpectedHttpStatusCodeException;
import com.universal.provider.message.entity.Sms;
import com.universal.provider.message.sms.SmsChannel;
import com.universal.provider.message.sms.SmsReporter;
import com.universal.provider.message.sms.SmsStatus;
import com.universal.provider.message.sms.sender.AbstractSmsSender;
import com.universal.utils.HttpUtils;

public class SmsChuanglanSender extends AbstractSmsSender implements Runnable {

    private String url = null;
    private String account = null;
    private String password = null;
    private SmsReporter reporter = null;

    private int interval = 10;
    private Thread thread = null;
    private Object lock = new Object();
    private BlockingQueue<Sms> queue = null;
    private volatile boolean stopped = false;

    private final static Logger logger = LoggerFactory.getLogger(SmsChuanglanSender.class);

    public SmsChuanglanSender(final String url, final String account, final String password) {

        this.url = url;
        this.account = account;
        this.password = password;

        queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void send(Sms sms) throws Exception {

        sms.setChannel(SmsChannel.CHUANGLAN);

        if (findSuccessedQuantityToday(sms, SmsChannel.CHUANGLAN) >= this.maxSendingTimes) {

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

        String status = StringUtils.EMPTY;

        Pattern pattern = Pattern.compile("\\d+,(\\d+)(?:\n\\d+)?");
        Matcher matcher = pattern.matcher(response.trim());
        if (matcher.find()) {
            status = matcher.group(1);
        } else {
            logger.error("Parse response failed.");
        }

        return status.equals("0");
    }

    @Override
    public void setReporter(SmsReporter reporter) {

        this.reporter = reporter;
    }

    @Override
    public boolean start() {

        logger.info("Starting Chuanglan sender ...");

        thread = new Thread(this);
        this.stopped = false;
        thread.start();

        logger.info("Sender started.");
        return true;
    }

    @Override
    public void stop() {

        logger.info("Stopping Chuanglan sender ...");
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

                Map<String, String> parameters = new HashMap<>();
                parameters.put("account", this.account);
                parameters.put("pswd", this.password);
                parameters.put("mobile", sms.getTo());
                parameters.put("msg", sms.getContent());
                parameters.put("needstatus", "true");

                try {

                    String response = HttpUtils.post(url, parameters);

                    logger.info("Response: " + response);

                    if (isSuccess(response)) {

                        logger.info(String.format("Send sms %s sucessfully.", sms));

                        sms.setStatus(SmsStatus.SNT);
                        this.reporter.onSubmittedSuccess(sms);
                    } else {

                        logger.error(String.format("Send sms %s failed.", sms));

                        sms.setStatus(SmsStatus.FAL);
                        this.reporter.onSubmittedFailed(sms);
                    }
                } catch (UnexpectedHttpStatusCodeException e) {

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

}
