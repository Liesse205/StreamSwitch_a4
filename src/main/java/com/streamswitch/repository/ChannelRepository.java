package com.streamswitch.repository;

import com.streamswitch.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Optional<Channel> findByNameIgnoreCase(String name);

    List<Channel> findByGenreIgnoreCase(String genre);

    boolean existsByNameIgnoreCase(String name);
}
