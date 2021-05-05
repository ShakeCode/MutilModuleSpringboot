package com.redis.service.service;

import com.redis.service.constant.Constant;
import com.redis.service.model.Student;
import com.redis.service.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

/**
 * The type Cache service.
 */
@Service
@CacheConfig(cacheNames = "student_cache")
public class CacheStudentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheStudentService.class);

    private final RedisUtil redisUtil;

    private static final String STUDENT_HASH_KEY = "student";

    /**
     * Instantiates a new Cache student service.
     * @param redisUtil the redis util
     */
    public CacheStudentService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 设置redis内存储的内容
     * @param key   the key
     * @param value the value
     * @return the string
     */
    @Cacheable(key = "#key")
    public String stringCache(String key, String value) {
        return value;
    }

    /**
     * 更新数据,更新缓存，不论是否存在对应缓存，每次调用都会查询数据库并且把返回值放入缓存
     * @param tenantCode the tenant code
     * @param student    the student
     * @return the student
     */
    @CachePut(key = "#tenantCode+'_'+#student.code")
    public Student updateStu(String tenantCode, Student student) {
        LOGGER.info("updateStu:{}", student);
        redisUtil.hset(String.join(Constant.UNDER_LINE, STUDENT_HASH_KEY, tenantCode), student.getCode(), student);
        return student;
    }

    /**
     * Add student by code result vo.更新缓存，不论是否存在对应缓存，每次调用都会查询数据库并且把返回值放入缓存
     * @param tenantCode the tenant code
     * @param student    the student
     * @return the result vo
     */
    @CachePut(key = "#tenantCode+'_'+#student.code")
    public Student addStudent(String tenantCode, Student student) {
        LOGGER.info("addStudentByCode:{}", student);
        redisUtil.hset(String.join(Constant.UNDER_LINE, STUDENT_HASH_KEY, tenantCode), student.getCode(), student);
        return student;
    }

    /**
     * 清除cache名字为emp下的所有数据
     */
    @CacheEvict(allEntries = true)
    public void clear() {
    }

    /**
     * Gets emp by last name.// @Caching 定义复杂的缓存规则，每次查询，都会执行下面的方法，因为里面使用了CachePut的方法
     * @param tenantCode the tenant code
     * @param code       the code
     * @return the emp by last name
     */
    @Caching(
            cacheable = {
                    @Cacheable(key = "#tenantCode+'_'+#code")
            },
            put = {
                    @CachePut(key = "#tenantCode+'_'+#code"),
            }
    )
    public Student queryStudentByCode(String tenantCode, String code) {
        Student student = (Student) redisUtil.hget(String.join(Constant.UNDER_LINE, STUDENT_HASH_KEY, tenantCode), code);
        return student;
    }
}
