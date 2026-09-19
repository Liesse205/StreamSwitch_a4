package com.streamswitch.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Channel name is required")
    @Size(max = 100, message = "Channel name must not exceed 100 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 20, message = "Channel number must not exceed 20 characters")
    private String channelNumber;

    @Size(max = 50, message = "Genre must not exceed 50 characters")
    private String genre; // e.g. "Sports", "News", "Entertainment"

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 255, message = "Logo URL must not exceed 255 characters")
    private String logoUrl;

    // A channel can belong to many subscription packages
    @ManyToMany(mappedBy = "channels", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("channels")
    @Builder.Default
    private List<SubscriptionPackage> packages = new ArrayList<>();
}
