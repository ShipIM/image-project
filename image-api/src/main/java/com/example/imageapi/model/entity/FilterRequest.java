package com.example.imageapi.model.entity;

import com.example.imageapi.model.enumeration.ImageStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "image_filter")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class FilterRequest {

    @Id
    private Long id;

    private ImageStatus status = ImageStatus.WIP;

    @Column("original_image_id")
    private String originalId;

    @Column("modified_image_id")
    private String modifiedId;

    @Column("request_id")
    private String requestId;

    @Column("user_id")
    private Long userId;

}
