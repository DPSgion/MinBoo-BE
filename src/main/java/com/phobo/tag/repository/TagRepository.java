package com.phobo.tag.repository;

import com.phobo.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    boolean existsByTagName(String tagName);

    Optional<Tag> findByTagName(String tagName);
}
