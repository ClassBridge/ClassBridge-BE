package com.linked.classbridge.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class JobScheduler {

    private final JobLauncher jobLauncher;

    private final Job tutorPaymentJob;

    @Scheduled(cron = "0 0 0 1 * ?")
    public void performSettlementJob() {
        try {
            jobLauncher.run(tutorPaymentJob, new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());
        } catch (Exception e) {
            log.error("batch scheduler error :: {}", e.getMessage());
        }
    }
}
