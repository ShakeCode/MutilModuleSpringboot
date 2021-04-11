package com.search.service.client;

import com.search.service.model.Song;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "DataServiceClient", url = "http://localhost:8888")
public interface DataServiceClient {

    @RequestMapping(value = "/listSong", method = RequestMethod.GET)
    List<Song> getSong();
}
