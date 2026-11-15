package com.daw.bibliotecadigitalg4.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter


@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class LibroExternoDTO {
    private val title: String? = null

    @JsonProperty("number_of_pages")
    private val numberOfPages: Int? = null

    @JsonProperty("publish_date")
    private val publishDate: String? = null

    private val publishers: MutableList<String?>? = null
}