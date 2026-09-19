package com.streamswitch.repository;

import com.streamswitch.entity.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, Long> {

    List<SubscriptionPackage> findByProviderId(Long providerId);

    List<SubscriptionPackage> findByActiveTrue();

    List<SubscriptionPackage> findByProviderIdAndActiveTrue(Long providerId);
}
