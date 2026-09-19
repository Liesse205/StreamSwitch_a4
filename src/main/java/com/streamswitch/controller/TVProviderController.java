package com.streamswitch.controller;

import com.streamswitch.entity.TVProvider;
import com.streamswitch.service.TVProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class TVProviderController {

    private final TVProviderService providerService;

    // POST - Create a new TV provider
    @PostMapping
    public ResponseEntity<TVProvider> createProvider(@Valid @RequestBody TVProvider provider) {
        TVProvider created = providerService.createProvider(provider);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // GET - Get all TV providers
    @GetMapping
    public ResponseEntity<List<TVProvider>> getAllProviders() {
        List<TVProvider> providers = providerService.getAllProviders();
        return ResponseEntity.ok(providers);
    }

    // GET - Get a TV provider by ID
    @GetMapping("/{id}")
    public ResponseEntity<TVProvider> getProviderById(@PathVariable Long id) {
        TVProvider provider = providerService.getProviderById(id);
        return ResponseEntity.ok(provider);
    }

    // PUT - Update a TV provider
    @PutMapping("/{id}")
    public ResponseEntity<TVProvider> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody TVProvider provider) {
        TVProvider updated = providerService.updateProvider(id, provider);
        return ResponseEntity.ok(updated);
    }

    // DELETE - Delete a TV provider
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        providerService.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }
}
