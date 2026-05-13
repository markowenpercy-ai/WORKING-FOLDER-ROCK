package com.go2super.service.jobs;

public interface OfflineJob {

    void setup();

    void run();

    long getInterval();

}
