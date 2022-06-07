package com.redis.service.controller;

import com.redis.service.annotation.LogTime;
import com.redis.service.model.ResultVO;
import com.redis.service.service.SmsQuotaService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Lock aspect test controller.
 */
@RequestMapping("v1/quota")
@RestController
public class LockAspectTestController {

    private final SmsQuotaService smsQuotaService;

    /**
     * Instantiates a new Lock aspect test controller.
     * @param smsQuotaService the sms quota service
     */
    public LockAspectTestController(SmsQuotaService smsQuotaService) {
        this.smsQuotaService = smsQuotaService;
    }

    /**
     * Take order result vo.
     * @return the result vo
     */
    @LogTime(operationMessage = "check sms quota controller ")
    @ApiOperation(value = "检查配额", notes = "检查配额")
    @RequestMapping(value = "check")
    public ResultVO<String> takeOrder() {
        return smsQuotaService.checkSmsQuota();
    }
}
