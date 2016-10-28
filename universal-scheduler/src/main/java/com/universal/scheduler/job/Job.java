package com.universal.scheduler.job;

public interface Job {

    void run();
    
    void run(String paramters);
}
