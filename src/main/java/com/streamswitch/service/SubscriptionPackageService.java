package com.streamswitch.service;

import com.streamswitch.entity.Channel;
import com.streamswitch.entity.SubscriptionPackage;
import com.streamswitch.entity.TVProvider;
import com.streamswitch.exception.ResourceNotFoundException;
import com.streamswitch.repository.ChannelRepository;
import com.streamswitch.repository.SubscriptionPackageRepository;
import com.streamswitch.repository.TVProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionPackageService {

    private final SubscriptionPackageRepository packageRepository;
    private final TVProviderRepository providerRepository;
    private final ChannelRepository channelRepository;

    // Create a new subscription package
    public SubscriptionPackage createPackage(SubscriptionPackage pkg, Long providerId) {
        TVProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "TV Provider not found with id: " + providerId
                ));

        pkg.setProvider(provider);
        pkg.setActive(pkg.getActive() != null ? pkg.getActive() : true);
        return packageRepository.save(pkg);
    }

    // Get all subscription packages
    public List<SubscriptionPackage> getAllPackages() {
        return packageRepository.findAll();
    }

    // Get a subscription package by ID
    public SubscriptionPackage getPackageById(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Subscription Package not found with id: " + id
                ));
    }

    // Get all packages for a specific provider
    public List<SubscriptionPackage> getPackagesByProvider(Long providerId) {
        if (!providerRepository.existsById(providerId)) {
            throw new ResourceNotFoundException(
                "TV Provider not found with id: " + providerId
            );
        }
        return packageRepository.findByProviderId(providerId);
    }

    // Update a subscription package
    public SubscriptionPackage updatePackage(Long id, SubscriptionPackage updatedPkg) {
        SubscriptionPackage existing = getPackageById(id);

        existing.setName(updatedPkg.getName());
        existing.setDescription(updatedPkg.getDescription());
        existing.setMonthlyPrice(updatedPkg.getMonthlyPrice());
        existing.setBillingCycle(updatedPkg.getBillingCycle());
        existing.setActive(updatedPkg.getActive());

        return packageRepository.save(existing);
    }

    // Delete a subscription package
    public void deletePackage(Long id) {
        SubscriptionPackage pkg = getPackageById(id);
        packageRepository.delete(pkg);
    }

    // Add a channel to a package
    public SubscriptionPackage addChannelToPackage(Long packageId, Long channelId) {
        SubscriptionPackage pkg = getPackageById(packageId);
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Channel not found with id: " + channelId
                ));

        // Business logic: prevent duplicate channel in the same package
        if (pkg.getChannels().contains(channel)) {
            throw new IllegalArgumentException(
                "Channel '" + channel.getName() + "' is already in package '" + pkg.getName() + "'"
            );
        }

        pkg.getChannels().add(channel);
        return packageRepository.save(pkg);
    }

    // Remove a channel from a package
    public SubscriptionPackage removeChannelFromPackage(Long packageId, Long channelId) {
        SubscriptionPackage pkg = getPackageById(packageId);
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Channel not found with id: " + channelId
                ));

        pkg.getChannels().remove(channel);
        return packageRepository.save(pkg);
    }
}
