package com.redis.service.controller;

import com.redis.service.model.ResultVO;
import com.redis.service.model.Student;
import com.redis.service.service.CacheStudentService;
import com.redis.service.utils.ThreadHeaderLocalUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Student controller.
 */
@RequestMapping("v1/student")
@RestController
public class StudentController {

    private final CacheStudentService studentService;

    /**
     * Instantiates a new Student controller.
     * @param studentService the student service
     */
    public StudentController(CacheStudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Query student by code result vo.
     * @param code the code
     * @return the result vo
     */
    @GetMapping(value = "/get")
    public ResultVO<Student> queryStudentByCode(@RequestParam("code") String code) {
        return ResultVO.successData(studentService.queryStudentByCode(ThreadHeaderLocalUtil.getHeaderInfo().getTenentCode(),code));
    }

    /**
     * Add student by code result vo.
     * @param student the student
     * @return the result vo
     */
    @PostMapping(value = "/add")
    public ResultVO<Student> addStudentByCode(@RequestBody Student student) {
        return ResultVO.successData(studentService.addStudent(ThreadHeaderLocalUtil.getHeaderInfo().getTenentCode(),student));
    }

    /**
     * Add student by code result vo.
     * @param student the student
     * @return the result vo
     */
    @PostMapping(value = "/update")
    public ResultVO<Student> updateStu(@RequestBody Student student) {
        return ResultVO.successData(studentService.updateStu(ThreadHeaderLocalUtil.getHeaderInfo().getTenentCode(),student));
    }
}
