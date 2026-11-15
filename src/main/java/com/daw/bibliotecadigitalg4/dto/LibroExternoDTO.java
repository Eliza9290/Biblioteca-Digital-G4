package com.daw.bibliotecadigitalg4.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibroExternoDTO {

    private String title;

    @JsonProperty("number_of_pages")
    private Integer numberOfPages;

    @JsonProperty("publish_date")
    private String publishDate;

    private List<String> publishers;
}
