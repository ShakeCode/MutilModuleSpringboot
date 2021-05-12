package com.task.service.controller;

import com.task.service.model.ResultVO;
import com.task.service.service.DynamicTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * The type Async task scheduler controller.
 */
@RequestMapping("v1/task/")
@Api("异步任务调度")
@RestController
public class AsyncTaskSchedulerController {

    private final DynamicTaskService dynamicTaskService;

    /**
     * Instantiates a new Async task scheduler controller.
     * @param dynamicTaskService the dynamic task service
     */
    public AsyncTaskSchedulerController(DynamicTaskService dynamicTaskService) {
        this.dynamicTaskService = dynamicTaskService;
    }

    /**
     * http://127.0.0.1:8092/task/v1/task/add
     * Add task result vo.
     * @param name the name
     * @param cron the cron
     * @return the result vo
     */
    @ApiOperation("新增任务调度")
    @GetMapping("/add")
    public ResultVO<Boolean> addTask(String name, String cron) {
        return ResultVO.successData(dynamicTaskService.addTask(name, cron));
    }

    /**
     * http://127.0.0.1:8092/task/v1/task/delete
     * Delete task result vo.
     * @param name the name
     * @return the result vo
     */
    @ApiOperation("删除任务调度")
    @GetMapping("/delete")
    public ResultVO<Boolean> deleteTask(String name) {
        return ResultVO.successData(dynamicTaskService.deleteTask(name));
    }
}
