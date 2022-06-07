package com.search.service.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * The type Sms quota result.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsQuotaResult {
    private Date sendTime;

    private Integer quota;
}
