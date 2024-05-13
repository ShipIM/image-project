package com.example.filter.dto.kafka.image;

import com.example.filter.model.enumeration.FilterType;
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
public class ImageFilterRequest implements Serializable {

    private String imageId;

    private String requestId;

    private List<FilterType> filters;

}
