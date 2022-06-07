package com.data.service.controller;

import com.data.service.model.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * The type Data controller.
 */
@RequestMapping("v1/data/")
@Api("用户索引")
@RestController
public class DataController {

    @Value("${server.port}")
    private String serverPort;


    /**
     * Gets song list.
     * @return the song list
     */
    @ApiOperation("获取服务端口")
    @GetMapping("/server/get")
    public ResultVO<String> getSongList() {
        return ResultVO.successData(this.serverPort);
    }

}
