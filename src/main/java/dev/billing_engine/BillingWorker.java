package dev.billing_engine;

import dev.billing_engine.dto.BillingResult;
import dev.invoices.data.InvoiceEntity;
import dev.invoices.data.InvoiceRepository;
import dev.invoices.InvoiceService;
import dev.invoices.InvoiceStatus;
import dev.invoices.dto.CreateInvoiceDto;
import dev.payments.PaymentGateway;
import dev.subscription_plans.BillingCycle;
import dev.subscriptions.data.SubscriptionEntity;
import dev.subscriptions.data.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.time.OffsetDateTime;

@Component
public class BillingWorker {
    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final PaymentGateway paymentGateway;
    private final SubscriptionRepository subscriptionRepository;

    public BillingWorker (InvoiceService invoiceService,
                          InvoiceRepository invoiceRepository,
                          PaymentGateway paymentGateway,
                          SubscriptionRepository subscriptionRepository) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.paymentGateway = paymentGateway;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BillingResult processSubscription(Long subscriptionId) {

        SubscriptionEntity subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription with id=" + subscriptionId + " not found."));



        String idempotencyKey = String.format("sub_%d_%s",
                subscription.getId(),
                subscription.getNextBillingDate().toLocalDate().toString());

        OffsetDateTime currentPeriodStart = subscription.getNextBillingDate();
        OffsetDateTime currentPeriodEnd = computeBillingPeriodEnd(currentPeriodStart, subscription.getSubscriptionPlan().getDuration());

        CreateInvoiceDto invoiceToCreate = new CreateInvoiceDto(
                subscription.getId(),
                subscription.getSubscriptionPlan().getCost(),
                InvoiceStatus.PENDING,
                currentPeriodStart,
                currentPeriodEnd,
                idempotencyKey
        );

        InvoiceEntity createdInvoice = invoiceService.createAndReturnEntity(invoiceToCreate, subscription.getTenant().getId());

        if (paymentGateway.processTransaction()) {
            createdInvoice.setStatus(InvoiceStatus.PAID);
            subscription.activate();
            subscription.resetPaymentAttempt();
            subscription.setNextBillingDate(currentPeriodEnd);

            invoiceRepository.saveAndFlush(createdInvoice);
            subscriptionRepository.saveAndFlush(subscription);

            return new BillingResult(subscription.getId(), createdInvoice.getId(), InvoiceStatus.PAID, "Success.");
        } else {

            createdInvoice.setStatus(InvoiceStatus.FAILED);
            if (subscription.incrementPaymentAttempt()) {
                subscription.markAsPastDue();
                subscription.updateNextRetryDate(currentPeriodStart);

            } else {
                subscription.cancel();

                invoiceRepository.saveAndFlush(createdInvoice);
                subscriptionRepository.saveAndFlush(subscription);

            }
            return new BillingResult(subscription.getId(), createdInvoice.getId(), InvoiceStatus.FAILED, "Payment failed.");
        }
    }

    private OffsetDateTime computeBillingPeriodEnd(OffsetDateTime currentPeriodStart, BillingCycle duration){
        if (duration.equals(BillingCycle.WEEKLY)) return currentPeriodStart.plusWeeks(1);
        else if (duration.equals(BillingCycle.MONTHLY)) return currentPeriodStart.plusMonths(1);
        else if (duration.equals(BillingCycle.QUARTERLY)) return currentPeriodStart.plusMonths(3);
        else if (duration.equals(BillingCycle.YEARLY)) return currentPeriodStart.plusYears(1);
        else throw new IllegalArgumentException("Constant named as " + duration + " not in BillingCycle enum.");
    }
}
