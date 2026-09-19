package com.streamswitch.service;

import com.streamswitch.entity.TVProvider;
import com.streamswitch.exception.ResourceNotFoundException;
import com.streamswitch.repository.TVProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TVProviderService {

    private final TVProviderRepository providerRepository;

    // Create a new TV provider
    public TVProvider createProvider(TVProvider provider) {
        // Business logic: prevent duplicate provider names
        if (providerRepository.existsByNameIgnoreCase(provider.getName())) {
            throw new IllegalArgumentException(
                "A TV provider with the name '" + provider.getName() + "' already exists"
            );
        }
        return providerRepository.save(provider);
    }

    // Get all TV providers
    public List<TVProvider> getAllProviders() {
        return providerRepository.findAll();
    }

    // Get a TV provider by ID
    public TVProvider getProviderById(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "TV Provider not found with id: " + id
                ));
    }

    // Update a TV provider
    public TVProvider updateProvider(Long id, TVProvider updatedProvider) {
        TVProvider existing = getProviderById(id);

        // Business logic: check if new name conflicts with another provider
        if (!existing.getName().equalsIgnoreCase(updatedProvider.getName())
                && providerRepository.existsByNameIgnoreCase(updatedProvider.getName())) {
            throw new IllegalArgumentException(
                "A TV provider with the name '" + updatedProvider.getName() + "' already exists"
            );
        }

        existing.setName(updatedProvider.getName());
        existing.setDescription(updatedProvider.getDescription());
        existing.setWebsiteUrl(updatedProvider.getWebsiteUrl());
        existing.setCountry(updatedProvider.getCountry());

        return providerRepository.save(existing);
    }

    // Delete a TV provider
    public void deleteProvider(Long id) {
        TVProvider provider = getProviderById(id);
        providerRepository.delete(provider);
    }
}
