package com.example.filter.dto.kafka.image;

import com.example.filter.model.enumeration.ImageStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImageDone {

    private String imageId;

    private String requestId;

    private ImageStatus status;

    private String message;

}
