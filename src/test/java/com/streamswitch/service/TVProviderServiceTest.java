package com.streamswitch.service;

import com.streamswitch.entity.TVProvider;
import com.streamswitch.exception.ResourceNotFoundException;
import com.streamswitch.repository.TVProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TVProviderService Unit Tests")
class TVProviderServiceTest {

    @Mock
    private TVProviderRepository providerRepository;

    @InjectMocks
    private TVProviderService providerService;

    private TVProvider testProvider;

    @BeforeEach
    void setUp() {
        testProvider = TVProvider.builder()
                .id(1L)
                .name("DStv")
                .description("MultiChoice satellite TV")
                .websiteUrl("https://www.dstv.com")
                .country("Nigeria")
                .build();
    }

    @Test
    @DisplayName("Should create a new TV provider successfully")
    void shouldCreateProvider() {
        when(providerRepository.existsByNameIgnoreCase("DStv")).thenReturn(false);
        when(providerRepository.save(any(TVProvider.class))).thenReturn(testProvider);

        TVProvider created = providerService.createProvider(testProvider);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("DStv");
        assertThat(created.getCountry()).isEqualTo("Nigeria");
        verify(providerRepository, times(1)).save(any(TVProvider.class));
    }

    @Test
    @DisplayName("Should get TV provider by ID")
    void shouldGetProviderById() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));

        TVProvider found = providerService.getProviderById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("DStv");
        assertThat(found.getWebsiteUrl()).isEqualTo("https://www.dstv.com");
    }

    @Test
    @DisplayName("Should throw exception when provider not found by ID")
    void shouldThrowExceptionWhenProviderNotFound() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> providerService.getProviderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found with id: 99");
    }
}
