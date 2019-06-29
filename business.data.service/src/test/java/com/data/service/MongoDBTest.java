package com.data.service;

import com.data.service.service.MongonDBService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MongoDBTest {

    @Autowired
    private MongonDBService mongonDBService;

    @Test
    public void testMongo() {
        Stream.iterate(2, a -> a + 2).limit(4).forEach(System.out::println);
//        mongonDBService.insertTest(new MongoModel(2,12,"lijun"));
//        mongonDBService.saveTest(new MongoModel(1,24,"xiaojun"));

        System.out.println(mongonDBService.findTestByName("lijun"));
//        System.out.println(mongonDBService.updateTest(new MongoModel(1,140,"halo顺风车")));

    }
}
