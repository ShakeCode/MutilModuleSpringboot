package com.search.service.controller;

import com.search.service.model.ResultVO;
import com.search.service.service.DataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * The type Data controller.   http://127.0.0.1:8091/search/v1/data/song/get
 */
@RequestMapping("v1/data/")
@Api("用户索引")
@RestController
public class DataController {

    private final DataService dataService;

    /**
     * Instantiates a new Data controller.
     * @param dataService the data service
     */
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * Gets song list.
     * @return the song list
     */
    @ApiOperation("获取歌单列表")
    @GetMapping("/song/get")
    public ResultVO getSongList() {
        return ResultVO.successData(dataService.getSongData());
    }

}
