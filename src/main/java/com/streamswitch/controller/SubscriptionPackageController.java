package com.streamswitch.controller;

import com.streamswitch.entity.SubscriptionPackage;
import com.streamswitch.service.SubscriptionPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class SubscriptionPackageController {

    private final SubscriptionPackageService packageService;

    // POST - Create a new subscription package under a provider
    @PostMapping("/provider/{providerId}")
    public ResponseEntity<SubscriptionPackage> createPackage(
            @PathVariable Long providerId,
            @Valid @RequestBody SubscriptionPackage pkg) {
        SubscriptionPackage created = packageService.createPackage(pkg, providerId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // GET - Get all subscription packages
    @GetMapping
    public ResponseEntity<List<SubscriptionPackage>> getAllPackages() {
        List<SubscriptionPackage> packages = packageService.getAllPackages();
        return ResponseEntity.ok(packages);
    }

    // GET - Get a subscription package by ID
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPackage> getPackageById(@PathVariable Long id) {
        SubscriptionPackage pkg = packageService.getPackageById(id);
        return ResponseEntity.ok(pkg);
    }

    // GET - Get all packages for a specific provider
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<SubscriptionPackage>> getPackagesByProvider(
            @PathVariable Long providerId) {
        List<SubscriptionPackage> packages = packageService.getPackagesByProvider(providerId);
        return ResponseEntity.ok(packages);
    }

    // PUT - Update a subscription package
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPackage> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPackage pkg) {
        SubscriptionPackage updated = packageService.updatePackage(id, pkg);
        return ResponseEntity.ok(updated);
    }

    // DELETE - Delete a subscription package
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }

    // POST - Add a channel to a package
    @PostMapping("/{packageId}/channels/{channelId}")
    public ResponseEntity<SubscriptionPackage> addChannelToPackage(
            @PathVariable Long packageId,
            @PathVariable Long channelId) {
        SubscriptionPackage updated = packageService.addChannelToPackage(packageId, channelId);
        return ResponseEntity.ok(updated);
    }

    // DELETE - Remove a channel from a package
    @DeleteMapping("/{packageId}/channels/{channelId}")
    public ResponseEntity<SubscriptionPackage> removeChannelFromPackage(
            @PathVariable Long packageId,
            @PathVariable Long channelId) {
        SubscriptionPackage updated = packageService.removeChannelFromPackage(packageId, channelId);
        return ResponseEntity.ok(updated);
    }
}
