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

    private ImageStatus status;

    @Column("original_image_id")
    private String original;

    @Column("modified_image_id")
    private String modified;

    @Column("request_id")
    private String request;

}
