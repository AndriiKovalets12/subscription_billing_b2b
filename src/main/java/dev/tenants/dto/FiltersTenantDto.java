package dev.tenants.dto;

public record FiltersTenantDto(
        String name
) {
    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                '}';
    }
}
