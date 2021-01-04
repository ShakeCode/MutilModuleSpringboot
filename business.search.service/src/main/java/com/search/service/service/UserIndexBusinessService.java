package com.search.service.service;

import com.alibaba.fastjson.JSON;
import com.search.service.model.ResultVO;
import com.search.service.model.UserVO;
import com.search.service.utils.BeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/*http://127.0.0.1:9200/promotion-user/_search*/
@Service
public class UserIndexBusinessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserIndexBusinessService.class);

    @Value("${user.index.name:promotion-user}")
    private String userIndexName;

    private final UserIndexService userIndexService;

    public UserIndexBusinessService(UserIndexService userIndexService) {
        this.userIndexService = userIndexService;
    }

    public ResultVO createUserIndex(String indexName) throws IOException {
        String index = indexName;
        if (StringUtils.isEmpty(index)) {
            index = userIndexName;
        }
        //如果存在就不创建了
        if (userIndexService.existsIndex(index)) {
            LOGGER.info(">>>index is already exist");
            return ResultVO.fail("索引已存在");
        }
        boolean flag = userIndexService.createIndex(index);
        if (flag) {
            LOGGER.info("创建索引库:{}成功！", index);
        }
        return ResultVO.success("创建用户索引成功");
    }

    public ResultVO deleteUserIndex(String indexName) throws IOException {
        boolean isAcknowledged = userIndexService.deleteIndex(indexName);
        LOGGER.info(">>>>delete index result:{}", isAcknowledged);
        return ResultVO.successData(isAcknowledged);
    }

    public ResultVO addUser(UserVO userVO) throws IOException {
        String id = userIndexService.add(userIndexName, JSON.toJSONString(userVO));
        return ResultVO.successData(id);
    }

    public ResultVO updateUser(UserVO userVO, String id) throws IOException {
        userIndexService.updateById(userIndexName, BeanUtil.beanToMap(userVO), id);
        return ResultVO.success();
    }


    public ResultVO bulkAddUser(List<Map<String, Object>> datas) throws IOException {
        userIndexService.bulkAdd(userIndexName, datas);
        return ResultVO.success();
    }

    public ResultVO queryUser() throws IOException {
        // 模糊查询
//        List<Map<String, Object>> dataList = userIndexService.queryMatch(userIndexName, "tags", "客户");
        // 精确查询
        List<Map<String, Object>> dataList = userIndexService.queryMatch(userIndexName, "tags.keyword", "高端客户");
//        List<Map<String, Object>> dataList = userIndexService.rangeQuery(userIndexName, "planSendTime", "2019-12-30 12:30:56", "2021-01-30 12:30:56");
//        List<Map<String, Object>> dataList = userIndexService.termQuery(userIndexName, "userId", "18707677500");
//        List<Map<String, Object>> dataList = userIndexService.termsQuery(userIndexName, "userId", Lists.newArrayList("18707657300", "13532817784"));
        return ResultVO.successData(dataList);
    }


}
