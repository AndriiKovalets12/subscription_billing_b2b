package dev.customers.dto;

public record CustomerFiltersDto(
        String firstName,
        String lastName,
        String email
) {
    @Override
    public String toString() {
        return  "{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
