package com.streamswitch.repository;

import com.streamswitch.entity.TVProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TVProviderRepository extends JpaRepository<TVProvider, Long> {

    Optional<TVProvider> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
