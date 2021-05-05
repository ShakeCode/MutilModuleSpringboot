package com.redis.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Student.
 */
@NoArgsConstructor
@Data
public class Student {

    private String code;

    /**
     * name
     */
    private String name;

    /**
     * address
     */
    private String address;
}
