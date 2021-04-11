package com.search.service.controller;

import com.search.service.model.ResultVO;
import com.search.service.service.AsyncSmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("v1/async/")
@Api("用户索引")
@RestController
public class AsyncController {

    private final AsyncSmsService asyncSmsService;

    public AsyncController(AsyncSmsService asyncSmsService) {
        this.asyncSmsService = asyncSmsService;
    }

    @ApiOperation("异步获取姓名")
    @GetMapping("/get")
    public ResultVO getName() {
        return ResultVO.successData(asyncSmsService.getName());
    }

}
