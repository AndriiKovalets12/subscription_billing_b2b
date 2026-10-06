package dev.subscriptions;

import dev.customers.CustomerEntity;
import dev.subscription_plans.SubscriptionPlanEntity;
import dev.tenants.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Table(name = "subscriptions")
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subscription_gen_seq")
    @SequenceGenerator(name = "subscription_gen_seq", sequenceName = "subscriptions_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlanEntity subscriptionPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Column(name = "start_date", nullable = false, updatable = false)
    private OffsetDateTime startDate;

    @Setter
    @Column(name = "next_billing_date", nullable = false)
    private OffsetDateTime nextBillingDate;

    @Column(name = "next_retry_date")
    private OffsetDateTime nextRetryDate;

    @Column(name = "status", nullable = false, length = 10)
    @Enumerated(value = EnumType.STRING)
    private SubscriptionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private  TenantEntity tenant;

    @Column(name = "payment_attempt", nullable = false)
    private Integer paymentAttempt;

    @Column(name = "version", nullable = false)
    @Version
    private Long version;

    protected SubscriptionEntity() {
    }

    public SubscriptionEntity(SubscriptionPlanEntity subscriptionPlan,
                              CustomerEntity customer,
                              OffsetDateTime nextBillingDate,
                              TenantEntity tenant) {
        this.subscriptionPlan = subscriptionPlan;
        this.customer = customer;
        this.startDate = OffsetDateTime.now();
        this.nextBillingDate = nextBillingDate;
        this.status = SubscriptionStatus.ACTIVE;
        this.tenant = tenant;
        this.paymentAttempt = 0;
        this.nextRetryDate = null;
        this.version = 1L;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
    }

    public void activate() {
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void markAsPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
    }

    public boolean incrementPaymentAttempt(){
        if (this.paymentAttempt != 4){
            this.paymentAttempt++;
            return true;
        }
        return false;
    }

    public void resetPaymentAttempt(){
        this.paymentAttempt = 0;
    }

    public void updateNextRetryDate(OffsetDateTime currentPeriodStart) {
        switch (this.paymentAttempt){
            case 1:
                this.nextRetryDate = currentPeriodStart.plusDays(1);
                break;
            case 2:
                this.nextRetryDate = currentPeriodStart.plusDays(2);
                break;
            case 3:
                this.nextRetryDate = currentPeriodStart.plusDays(5);
                break;
        }
    }

}
