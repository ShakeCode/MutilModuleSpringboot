package com.search.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Song {
    private Integer id;
    private Integer songId;
    private Integer songListId;
}
