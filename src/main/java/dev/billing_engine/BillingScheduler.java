package dev.billing_engine;

import dev.billing_engine.dto.BillingReportDto;
import dev.billing_engine.dto.BillingResult;
import dev.invoices.InvoiceStatus;
import dev.subscriptions.data.SubscriptionEntity;
import dev.subscriptions.data.SubscriptionRepository;
import dev.subscriptions.SubscriptionStatus;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class BillingScheduler {
    private final SubscriptionRepository subscriptionRepository;
    private final BillingWorker billingWorker;
    private final ExecutorService billingExecutor;

    public BillingScheduler(SubscriptionRepository subscriptionRepository, BillingWorker billingWorker, ExecutorService billingExecutor) {
        this.subscriptionRepository = subscriptionRepository;
        this.billingWorker = billingWorker;
        this.billingExecutor = billingExecutor;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public BillingReportDto runDailyBilling(){
        int pageSize = 100;
        OffsetDateTime now = OffsetDateTime.now();
        Page<SubscriptionEntity> page;

        List<BillingResult> allResults = new ArrayList<>();
        do {
            page = subscriptionRepository.findActiveSubscriptionsByBilling(
                    now,
                    SubscriptionStatus.ACTIVE,
                    PageRequest.of(0, pageSize)
            );

            addToAllResults(page, allResults);

        } while (!page.isEmpty());

        do {
            page = subscriptionRepository.findPastDueSubscriptionsByBilling(
                    now,
                    SubscriptionStatus.PAST_DUE,
                    PageRequest.of(0, pageSize)
            );

            addToAllResults(page, allResults);

        } while (!page.isEmpty());

        return generateReport(allResults);
    }

    private void addToAllResults(Page<SubscriptionEntity> page, List<BillingResult> allResults) {
        List<CompletableFuture<BillingResult>> futures = page.getContent().stream()
                .map(sub -> CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return billingWorker.processSubscription(sub.getId());

                            } catch(ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                                log.warn("Concurrency conflict for subscription {}. Rolling back.", sub.getId());
                                return new BillingResult(sub.getId(), null, null, "Skipped: Duplicate idempotency key");

                            } catch(Exception e){
                                log.error("Failed to process billing for subscription {}", sub.getId(), e);
                                throw new RuntimeException("Billing process failed", e);
                            }
                        },
                        billingExecutor
                ))
                .toList();

        List<BillingResult> batchResults = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        allResults.addAll(batchResults);
    }

    private BillingReportDto generateReport(List<BillingResult> resultList){
        Long success = resultList.stream().filter(res -> res.status().equals(InvoiceStatus.PAID)).count();
        Long failed = resultList.stream().filter(res -> res.status().equals(InvoiceStatus.FAILED)).count();

        return new BillingReportDto((long) resultList.size(), success, failed, resultList);
    }
}
