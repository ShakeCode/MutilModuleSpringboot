package com.search.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.search.service.service.UserIndexService;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The type User index service.
 */
@Service
public class UserIndexServiceImpl implements UserIndexService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserIndexServiceImpl.class);

    private static final int BATCH_COUNT = 1;

    private final RestHighLevelClient restHighLevelClient;

    /**
     * Instantiates a new User index service.
     * @param restHighLevelClient the rest high level client
     */
    public UserIndexServiceImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public boolean createIndex(String indexName) throws IOException {
        // 开始创建库
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        //配置文件
        // File jsonFile = ResourceUtils.getFile("classpath:promotionUser.json");
        File jsonFile = ResourceUtils.getFile("classpath:user.json");
        String json = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8.name());
        request.source(json, XContentType.JSON);
        // request.mapping(json, XContentType.JSON);
        request.alias(new Alias("user-data"));
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        return createIndexResponse.isAcknowledged();
    }

    @Override
    public boolean existsIndex(String indexName) throws IOException {
        GetIndexRequest getRequest = new GetIndexRequest(indexName);
        getRequest.local(false);
        getRequest.humanReadable(true);
        return restHighLevelClient.indices().exists(getRequest, RequestOptions.DEFAULT);
    }

    @Override
    public boolean deleteIndex(String indexName) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        AcknowledgedResponse deleteResponse = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        return deleteResponse.isAcknowledged();
    }

    @Override
    public String add(String index, String json) throws IOException {
        IndexRequest indexRequest = new IndexRequest(index);
        indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        indexRequest.source(json, XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        LOGGER.info("add result: {},status:{}", JSON.toJSONString(indexResponse), indexResponse.status());
        return indexResponse.getId();
    }

    @Override
    public void bulkAdd(String index, List<Map<String, Object>> dataList) throws IOException {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        long time = 0;
        List<List<Map<String, Object>>> partList = Lists.partition(dataList, BATCH_COUNT);
        for (List<Map<String, Object>> datas : partList) {
            LOGGER.info(">>>batch add size:{}", datas.size());
            for (Map<String, Object> data : datas) {
                BulkRequest request = new BulkRequest();
                request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                request.add(new IndexRequest(index).source(data, XContentType.JSON));
                BulkResponse responses = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
                if (responses.hasFailures()) {
                    Stream.of(responses.getItems()).forEach(res -> LOGGER.error("新增失败：{}", res.getFailure().getCause().getMessage()));
                }
                time += responses.getTook().duration();
            }
        }
        LOGGER.info(">>>batch add user waste time:{}ms", time);
    }

    @Override
    public UpdateResponse updateById(String index, Map<String, Object> map, String id) throws IOException {
        UpdateRequest request = new UpdateRequest(index, id).doc(map);
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        LOGGER.info("update result: {},status:{}", JSON.toJSONString(updateResponse), updateResponse.status());
        return updateResponse;
    }

    @Override
    public List<Map<String, Object>> queryMatch(String indexName, String field, String keyWord) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery(field, keyWord));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        writeTotal(searchRequest, hits);
        SearchHit[] hitsArr = hits.getHits();
        return dealResultList(hitsArr);
    }

    @Override
    public List<Map<String, Object>> termQuery(String indexName, String fieldName, String fieldValue) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        searchRequest.source(sourceBuilder);
        sourceBuilder.query(QueryBuilders.termQuery(fieldName, fieldValue));
        //分页
        sourceBuilder.from(0);
        sourceBuilder.size(10);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        writeTotal(searchRequest, hits);
        SearchHit[] hitsArr = hits.getHits();
        return dealResultList(hitsArr);
    }

    @Override
    public List<Map<String, Object>> termsQuery(String indexName, String fieldName, List<String> fieldValues) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termsQuery(fieldName, fieldValues));
        sourceBuilder.from(0);
        sourceBuilder.size(10);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        writeTotal(searchRequest, hits);
        SearchHit[] hitsArr = hits.getHits();
        return dealResultList(hitsArr);
    }


    @Override
    public List<Map<String, Object>> rangeQuery(String indexName, String fieldName, String from, String to) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.rangeQuery(fieldName).from(from).to(to));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        writeTotal(searchRequest, hits);
        SearchHit[] hitsArr = hits.getHits();
        return dealResultList(hitsArr);
    }

    @Override
    public void deleteByQuery(String index, QueryBuilder queryBuilder) throws IOException {
        // 在一组索引上创建DeleteByQueryRequest
        DeleteByQueryRequest request = new DeleteByQueryRequest(index, "source2");
        //设置版本冲突时继续
        request.setConflicts("proceed");
        // request.setQuery(new TermQueryBuilder("userId", "kimchy"));
        request.setQuery(queryBuilder);
        request.setBatchSize(10000);
        BulkByScrollResponse response = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        LOGGER.info("deleteByQuery waste:{}", response.getTook());
    }

    /**
     * Update by query bulk by scroll response.
     * @param index        the index
     * @param queryBuilder the query builder
     * @return the bulk by scroll response
     * @throws IOException the io exception
     */
    @Override
    public BulkByScrollResponse updateByQuery(String index, QueryBuilder queryBuilder) throws IOException {
        UpdateByQueryRequest request = new UpdateByQueryRequest(index);
        //设置版本冲突时继续
        request.setConflicts("proceed");
        request.setQuery(queryBuilder);
        BulkByScrollResponse updateResponse = restHighLevelClient.updateByQuery(request, RequestOptions.DEFAULT);
        LOGGER.info("update by query result: {},status:{}", JSON.toJSONString(updateResponse), updateResponse.getStatus());
        return updateResponse;
    }

    private List<Map<String, Object>> dealResultList(SearchHit[] hitsArr) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (SearchHit hit : hitsArr) {
            response.add(hit.getSourceAsMap());
        }
        return response;
    }

    private void writeTotal(SearchRequest searchRequest, SearchHits hits) {
        LOGGER.info("source:{}", searchRequest.source());
        LOGGER.info("count:{}", hits.getTotalHits());
    }

}

