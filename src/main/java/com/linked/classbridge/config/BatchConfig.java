package com.linked.classbridge.config;

import com.linked.classbridge.service.TutorPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final TutorPaymentService tutorPaymentService;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    @Bean
    public Job tutorPaymentJob() {
        return new JobBuilder("tutorPaymentJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(paymentStep())
                .build();
    }

    @Bean
    public Step paymentStep() {
        return new StepBuilder("paymentStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    tutorPaymentService.processMonthlySettlement();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
