package com.universal.provider.file;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.config.ApplicationConfig;

/**
 * file provider main
 *
 */
public class Application {

    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    private static volatile boolean stopped = false;
    private final static Object lock = new Object();

    public static void main(String[] args) throws IOException {

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml", "applicationContext-provider.xml" });
        context.registerShutdownHook();
        context.start();

        final ApplicationConfig applicationConfig = context.getBean(ApplicationConfig.class);

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

        logger.info("{} is running ...", applicationConfig.getName());

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

        logger.info("{} has been stopped.", applicationConfig.getName());
    }
}
