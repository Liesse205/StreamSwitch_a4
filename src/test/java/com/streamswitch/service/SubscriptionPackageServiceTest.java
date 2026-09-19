package com.streamswitch.service;

import com.streamswitch.entity.SubscriptionPackage;
import com.streamswitch.entity.TVProvider;
import com.streamswitch.exception.ResourceNotFoundException;
import com.streamswitch.repository.ChannelRepository;
import com.streamswitch.repository.SubscriptionPackageRepository;
import com.streamswitch.repository.TVProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionPackageService Unit Tests")
class SubscriptionPackageServiceTest {

    @Mock
    private SubscriptionPackageRepository packageRepository;

    @Mock
    private TVProviderRepository providerRepository;

    @Mock
    private ChannelRepository channelRepository;

    @InjectMocks
    private SubscriptionPackageService packageService;

    private TVProvider testProvider;
    private SubscriptionPackage testPackage;

    @BeforeEach
    void setUp() {
        testProvider = TVProvider.builder()
                .id(1L)
                .name("DStv")
                .description("MultiChoice satellite TV")
                .country("Nigeria")
                .build();

        testPackage = SubscriptionPackage.builder()
                .id(1L)
                .name("DStv Access")
                .description("Mid-tier package with sports and news")
                .monthlyPrice(new BigDecimal("4500.00"))
                .billingCycle("monthly")
                .active(true)
                .provider(testProvider)
                .channels(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should create a package under a provider")
    void shouldCreatePackage() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        when(packageRepository.save(any(SubscriptionPackage.class))).thenReturn(testPackage);

        SubscriptionPackage created = packageService.createPackage(testPackage, 1L);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("DStv Access");
        assertThat(created.getMonthlyPrice()).isEqualByComparingTo(new BigDecimal("4500.00"));
        assertThat(created.getProvider()).isEqualTo(testProvider);
        verify(packageRepository, times(1)).save(any(SubscriptionPackage.class));
    }

    @Test
    @DisplayName("Should get package by ID")
    void shouldGetPackageById() {
        when(packageRepository.findById(1L)).thenReturn(Optional.of(testPackage));

        SubscriptionPackage found = packageService.getPackageById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("DStv Access");
        assertThat(found.getActive()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when creating package for non-existent provider")
    void shouldThrowExceptionWhenProviderNotFound() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> packageService.createPackage(testPackage, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("TV Provider not found with id: 99");
    }
}
