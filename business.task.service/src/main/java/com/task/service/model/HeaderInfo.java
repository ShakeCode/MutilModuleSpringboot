package com.task.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Header info.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class HeaderInfo {

    private String tenentCode;
    /**
     * gcToken
     */
    private String gcToken;
    /**
     * userId
     */
    private String userId;
    /**
     * userName
     */
    private String userName;
}
