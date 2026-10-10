package dev.users.security;

public enum UserAuthority {
    // TENANT_SUPPORT authorities
    READ_CUSTOMERS,
    READ_INVOICES,

    // TENANT_ADMIN authorities
    READ_ANALYTICS,
    CREATE_PLAN,
    DELETE_PLAN,
    CREATE_SUBSCRIPTIONS,
    CANCEL_SUBSCRIPTIONS,

    // SUPER_ADMIN authorities
    START_BILLING,
    CREATE_TENANTS,
    DELETE_TENANTS
}
