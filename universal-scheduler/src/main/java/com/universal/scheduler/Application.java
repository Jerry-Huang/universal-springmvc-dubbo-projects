package com.universal.scheduler;

import java.io.IOException;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.universal.scheduler.job.Job;

public class Application {

    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    private static volatile boolean stopped = false;
    private final static Object lock = new Object();

    public static void main(String[] args) throws IOException, SchedulerException {

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "applicationContext.xml", "applicationContext-consumer.xml", "applicationContext-mybatis.xml", "applicationContext-redis.xml" });

        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                try {
                    context.close();
                    stopped = true;
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        context.registerShutdownHook();
        context.start();

        logger.info("Scheduler is running ...");

        if (args.length >= 1) {

            String jobClassName = args[0];
            Object job = context.getBean(jobClassName);

            if (job instanceof Job) {
                logger.info("Manual run {} ...", jobClassName);
                if (args.length >= 2) {
                    ((Job) job).run(args[1]);
                } else {
                    ((Job) job).run();
                }
            }
        }

        while (!stopped) {
            synchronized (lock) {
                try {
                    lock.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (stopped) {
                    break;
                }
            }
        }

        logger.info("Scheduler has been stopped.");
    }
}
