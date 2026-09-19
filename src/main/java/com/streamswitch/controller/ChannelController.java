package com.streamswitch.controller;

import com.streamswitch.entity.Channel;
import com.streamswitch.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    // POST - Create a new channel
    @PostMapping
    public ResponseEntity<Channel> createChannel(@Valid @RequestBody Channel channel) {
        Channel created = channelService.createChannel(channel);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // GET - Get all channels
    @GetMapping
    public ResponseEntity<List<Channel>> getAllChannels() {
        List<Channel> channels = channelService.getAllChannels();
        return ResponseEntity.ok(channels);
    }

    // GET - Get a channel by ID
    @GetMapping("/{id}")
    public ResponseEntity<Channel> getChannelById(@PathVariable Long id) {
        Channel channel = channelService.getChannelById(id);
        return ResponseEntity.ok(channel);
    }

    // GET - Get channels by genre
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Channel>> getChannelsByGenre(@PathVariable String genre) {
        List<Channel> channels = channelService.getChannelsByGenre(genre);
        return ResponseEntity.ok(channels);
    }

    // PUT - Update a channel
    @PutMapping("/{id}")
    public ResponseEntity<Channel> updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody Channel channel) {
        Channel updated = channelService.updateChannel(id, channel);
        return ResponseEntity.ok(updated);
    }

    // DELETE - Delete a channel
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long id) {
        channelService.deleteChannel(id);
        return ResponseEntity.noContent().build();
    }
}
