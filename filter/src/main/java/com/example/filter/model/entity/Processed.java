package com.example.filter.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "processed")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Processed {

    @Id
    private Long id;

    @Column("original_image_id")
    private String original;

    @Column("request_id")
    private String request;

}
