package com.streamswitch.service;

import com.streamswitch.entity.Channel;
import com.streamswitch.exception.ResourceNotFoundException;
import com.streamswitch.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;

    // Create a new channel
    public Channel createChannel(Channel channel) {
        // Business logic: prevent duplicate channel names
        if (channelRepository.existsByNameIgnoreCase(channel.getName())) {
            throw new IllegalArgumentException(
                "A channel with the name '" + channel.getName() + "' already exists"
            );
        }
        return channelRepository.save(channel);
    }

    // Get all channels
    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    // Get a channel by ID
    public Channel getChannelById(Long id) {
        return channelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Channel not found with id: " + id
                ));
    }

    // Get channels by genre
    public List<Channel> getChannelsByGenre(String genre) {
        return channelRepository.findByGenreIgnoreCase(genre);
    }

    // Update a channel
    public Channel updateChannel(Long id, Channel updatedChannel) {
        Channel existing = getChannelById(id);

        // Business logic: check if new name conflicts with another channel
        if (!existing.getName().equalsIgnoreCase(updatedChannel.getName())
                && channelRepository.existsByNameIgnoreCase(updatedChannel.getName())) {
            throw new IllegalArgumentException(
                "A channel with the name '" + updatedChannel.getName() + "' already exists"
            );
        }

        existing.setName(updatedChannel.getName());
        existing.setChannelNumber(updatedChannel.getChannelNumber());
        existing.setGenre(updatedChannel.getGenre());
        existing.setDescription(updatedChannel.getDescription());
        existing.setLogoUrl(updatedChannel.getLogoUrl());

        return channelRepository.save(existing);
    }

    // Delete a channel
    public void deleteChannel(Long id) {
        Channel channel = getChannelById(id);
        channelRepository.delete(channel);
    }
}
