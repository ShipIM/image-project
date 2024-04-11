package com.example.imageproject.model.entity;

import com.example.imageproject.model.enumeration.ImageStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "image_filter")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
