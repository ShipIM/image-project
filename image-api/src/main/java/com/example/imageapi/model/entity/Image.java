package com.example.imageapi.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "image")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Image {

    @Id
    private Long id;

    private String filename;

    private Long size;

    @Column("image_id")
    private String imageId;

    @Column("user_id")
    private Long userId;

}