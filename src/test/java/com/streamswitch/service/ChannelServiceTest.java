package com.streamswitch.service;

import com.streamswitch.entity.Channel;
import com.streamswitch.exception.ResourceNotFoundException;
import com.streamswitch.repository.ChannelRepository;
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
@DisplayName("ChannelService Unit Tests")
class ChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @InjectMocks
    private ChannelService channelService;

    private Channel testChannel;

    @BeforeEach
    void setUp() {
        testChannel = Channel.builder()
                .id(1L)
                .name("SuperSport")
                .channelNumber("158")
                .genre("Sports")
                .description("Premier African sports channel")
                .logoUrl("https://example.com/supersport.png")
                .build();
    }

    @Test
    @DisplayName("Should create a new channel successfully")
    void shouldCreateChannel() {
        when(channelRepository.existsByNameIgnoreCase("SuperSport")).thenReturn(false);
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        Channel created = channelService.createChannel(testChannel);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("SuperSport");
        assertThat(created.getGenre()).isEqualTo("Sports");
        verify(channelRepository, times(1)).save(any(Channel.class));
    }

    @Test
    @DisplayName("Should get channel by ID")
    void shouldGetChannelById() {
        when(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel));

        Channel found = channelService.getChannelById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("SuperSport");
        assertThat(found.getChannelNumber()).isEqualTo("158");
    }

    @Test
    @DisplayName("Should throw exception when channel not found by ID")
    void shouldThrowExceptionWhenChannelNotFound() {
        when(channelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.getChannelById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found with id: 99");
    }
}
