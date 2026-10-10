package dev.tenants.data;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "tenants")
public class TenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tenants_seq_gen")
    @SequenceGenerator(name = "tenants_seq_gen", sequenceName = "tenants_seq")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 75)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public TenantEntity(String name) {
        this.name = name;
        this.isActive = true;
    }

    protected TenantEntity() {}

    public void updateName(String newName){
        this.name = newName;
    }

    public void deactivate(){
        this.isActive = false;
    }
}
