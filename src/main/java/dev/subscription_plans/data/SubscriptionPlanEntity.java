package dev.subscription_plans.data;

import dev.subscription_plans.BillingCycle;
import dev.tenants.data.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subscription_plans_seq_gen")
    @SequenceGenerator(name = "subscription_plans_seq_gen", sequenceName = "subscription_plans_seq")
    private Long id;

    @Column(name = "name", nullable = false, length = 25)
    private String name;

    @Column(name = "cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal cost;

    @Column(name = "duration", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingCycle duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "version", nullable = false)
    @Version
    private Long version;


    public SubscriptionPlanEntity(String name,
                                  BigDecimal cost,
                                  BillingCycle duration,
                                  TenantEntity tenant,
                                  boolean isActive) {
        this.name = name;
        this.cost = cost;
        this.duration = duration;
        this.tenant = tenant;
        this.isActive = isActive;
    }

    protected SubscriptionPlanEntity() {
    }

    public void archive(){
        isActive = false;
    }

    public void updateName(String newName){
        if (!(newName == null || newName.isEmpty())) {
            this.name = newName;
        }
    }

}

