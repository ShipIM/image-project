package com.example.imageproject.repository;

import com.example.imageproject.model.entity.Image;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends CrudRepository<Image, Long> {

    Optional<Image> findByImageId(String imageId);

    List<Image> findAllByUserId(Long userId);

    @Query("select user_id " +
            "from image " +
            "where image_id = :imageId")
    Optional<Long> findUserByImageId(String imageId);

    @Modifying
    @Query("delete from image " +
            "where image_id = :imageId")
    void removeByImageId(String imageId);

    List<Image> findAll();

}
