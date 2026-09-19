package com.streamswitch.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscription_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Package name is required")
    @Size(max = 100, message = "Package name must not exceed 100 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Monthly price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Monthly price format is invalid")
    @Column(nullable = false)
    private BigDecimal monthlyPrice;

    @Size(max = 50, message = "Billing cycle must not exceed 50 characters")
    private String billingCycle; // e.g. "monthly", "yearly"

    private Boolean active;

    // Many packages belong to one provider
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    @JsonIgnoreProperties({"packages", "hibernateLazyInitializer"})
    private TVProvider provider;

    // A package can have many channels (many-to-many)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "package_channels",
        joinColumns = @JoinColumn(name = "package_id"),
        inverseJoinColumns = @JoinColumn(name = "channel_id")
    )
    @JsonIgnoreProperties("packages")
    @Builder.Default
    private List<Channel> channels = new ArrayList<>();
}
