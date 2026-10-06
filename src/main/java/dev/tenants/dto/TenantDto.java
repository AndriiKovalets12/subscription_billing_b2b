package dev.tenants.dto;

public record TenantDto(
        Long id,
        String name
) {
    @Override
    public String toString() {
        return  '{' +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
