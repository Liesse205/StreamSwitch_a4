package com.streamswitch.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tv_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TVProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Provider name is required")
    @Size(max = 100, message = "Provider name must not exceed 100 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    private String websiteUrl;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    // A provider has many subscription packages
    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("provider")
    @Builder.Default
    private List<SubscriptionPackage> packages = new ArrayList<>();
}
