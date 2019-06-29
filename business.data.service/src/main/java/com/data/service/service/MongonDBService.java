package com.data.service.service;

import com.data.service.domain.MongoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongonDBService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 创建对象
     */
    public void saveTest(MongoModel test) {
        mongoTemplate.save(test, "myDB");
    }

    /**
     * 根据用户名查询对象
     *
     * @return
     */
    public MongoModel findTestByName(String name) {
        Query query = new Query(Criteria.where("name").is(name));
        MongoModel mgt = mongoTemplate.findOne(query, MongoModel.class, "myDB");
        return mgt;
    }

    public List<MongoModel> findAll() {
       return mongoTemplate.findAll(MongoModel.class, "myDB");
    }

    /**
     * 更新对象
     */
    public int updateTest(MongoModel test) {
        Query query = new Query(Criteria.where("id").is(test.getId()));
        Update update = new Update().set("age", test.getAge()).set("name", test.getName());
        //更新查询返回结果集的第一条
        return mongoTemplate.updateFirst(query, update, MongoModel.class, "myDB").getN();
        //更新查询返回结果集的所有
        // mongoTemplate.updateMulti(query,update,TestEntity.class);
    }

    /**
     * 删除对象
     *
     * @param id
     */
    public void deleteTestById(Integer id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, MongoModel.class);
    }

    public void insertTest(MongoModel mongoModel) {
        mongoTemplate.insert(mongoModel, "myDB");
    }
}
