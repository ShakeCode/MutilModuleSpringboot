package com.search.service.service;

import org.elasticsearch.action.update.UpdateResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The interface User index service.
 * @since 2021-01-04
 */
public interface UserIndexService {

    /**
     * Create index boolean.
     * @param indexName the index name
     * @return the boolean
     */
    boolean createIndex(String indexName) throws IOException;

    /**
     * Exists index boolean.
     * @param indexName the index name
     * @return the boolean
     */
    boolean existsIndex(String indexName) throws IOException;

    /**
     * Delete index boolean.
     * @param indexName the index name
     * @return the boolean
     */
    boolean deleteIndex(String indexName) throws IOException;

    String add(String index, String json) throws IOException;

    void bulkAdd(String index, List<Map<String, Object>> dataList) throws IOException;

    UpdateResponse updateById(String index, Map<String, Object> map, String id) throws IOException;

    List<Map<String, Object>> queryMatch(String indexName, String field, String keyWord) throws IOException;

    List<Map<String, Object>> termQuery(String indexName, String fieldName, String fieldValue) throws IOException;

    List<Map<String, Object>> termsQuery(String indexName, String fieldName, List<String> fieldValues) throws IOException;

    List<Map<String, Object>> rangeQuery(String indexName, String fieldName, String from, String to) throws IOException;
}
