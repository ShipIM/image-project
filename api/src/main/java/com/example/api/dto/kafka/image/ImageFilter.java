package com.example.api.dto.kafka.image;

import com.example.api.model.enumeration.FilterType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImageFilter implements Serializable {

    private String imageId;

    private String requestId;

    private List<FilterType> filters;

}
