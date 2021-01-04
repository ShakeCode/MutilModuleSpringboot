package com.search.service.controller;

import com.search.service.model.ResultVO;
import com.search.service.model.UserVO;
import com.search.service.service.UserIndexBusinessService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequestMapping("v1/index/user")
@Api("用户索引")
@RestController
public class UserIndexController {

    private final UserIndexBusinessService userIndexBusinessService;

    public UserIndexController(UserIndexBusinessService userIndexBusinessService) {
        this.userIndexBusinessService = userIndexBusinessService;
    }

    @ApiOperation("创建用户索引库")
    @GetMapping("/create")
    public ResultVO createUserIndex(@RequestParam(value = "indexName", required = false) String indexName) throws IOException {
        return userIndexBusinessService.createUserIndex(indexName);
    }

    @ApiOperation("删除用户索引库")
    @GetMapping("/delete")
    public ResultVO deleteIndex(@RequestParam(value = "indexName", required = true) String indexName) throws IOException {
        return userIndexBusinessService.deleteUserIndex(indexName);
    }

    @ApiOperation("新增用户")
    @PostMapping("/add")
    public ResultVO addUser(@RequestBody UserVO userVO) throws IOException {
        return userIndexBusinessService.addUser(userVO);
    }

    @ApiOperation("修改用户")
    @PostMapping("/update")
    public ResultVO updateUser(@RequestBody UserVO userVO, @RequestParam("id") String id) throws IOException {
        return userIndexBusinessService.updateUser(userVO, id);
    }

    @ApiOperation("批量新增用户")
    @PostMapping("/batchAdd")
    public ResultVO bulkAddUser(@RequestBody List<Map<String, Object>> datas) throws IOException {
        return userIndexBusinessService.bulkAddUser(datas);
    }

    @ApiOperation("查询用户")
    @PostMapping("/query")
    public ResultVO queryUser() throws IOException {
        return userIndexBusinessService.queryUser();
    }
}
