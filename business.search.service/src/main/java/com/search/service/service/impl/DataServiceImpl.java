package com.search.service.service.impl;

import com.search.service.client.DataServiceClient;
import com.search.service.model.Song;
import com.search.service.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataServiceImpl.class);

    private DataServiceClient dataServiceClient;

    public DataServiceImpl(DataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    @Override
    public List<Song> getSong() {
        return dataServiceClient.getSong();
    }

    @Async("asyncExecutor")
    public List<Song> getSongData() {
        LOGGER.info("song list:{}", this.getSong());
        return new ArrayList<>();
    }
}
