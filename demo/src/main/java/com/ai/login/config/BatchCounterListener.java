package com.ai.login.config;

import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BatchCounterListener
        implements JobExecutionListener, StepExecutionListener {

    private final AtomicInteger totalRead    = new AtomicInteger(0);
    private final AtomicInteger totalSaved   = new AtomicInteger(0);
    private final AtomicInteger totalSkipped = new AtomicInteger(0);

    // ── Reset before every job ────────────────────────────────────────────
    @Override
    public void beforeJob(JobExecution jobExecution) {
        totalRead.set(0);
        totalSaved.set(0);
        totalSkipped.set(0);
        System.out.println(">>> Batch Job Started: " + jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // Push counters into job execution context so controller can read them
        jobExecution.getExecutionContext().putInt("totalRead",    totalRead.get());
        jobExecution.getExecutionContext().putInt("totalSaved",   totalSaved.get());
        jobExecution.getExecutionContext().putInt("totalSkipped", totalSkipped.get());

        System.out.println(">>> Batch Job Finished");
        System.out.println("    Total Read:    " + totalRead.get());
        System.out.println("    Total Saved:   " + totalSaved.get());
        System.out.println("    Total Skipped: " + totalSkipped.get());
        System.out.println("    Status:        " + jobExecution.getStatus());
    }

    // ── Step listeners ────────────────────────────────────────────────────
    @Override
    public void beforeStep(StepExecution stepExecution) {}

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        totalRead.addAndGet((int) stepExecution.getReadCount());
        totalSaved.addAndGet((int) stepExecution.getWriteCount());
        totalSkipped.addAndGet(
            (int)(stepExecution.getReadSkipCount()
                + stepExecution.getProcessSkipCount()
                + stepExecution.getWriteSkipCount())
        );
        return stepExecution.getExitStatus();
    }

    // ── Getters for controller ────────────────────────────────────────────
    public int getTotalRead()    { return totalRead.get(); }
    public int getTotalSaved()   { return totalSaved.get(); }
    public int getTotalSkipped() { return totalSkipped.get(); }
}