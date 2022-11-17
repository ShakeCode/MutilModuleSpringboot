package com.search.service.service;

import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The interface User index service.
 * @since 2021 -01-04
 */
public interface UserIndexService {

    /**
     * Create index boolean.
     * @param indexName the index name
     * @return the boolean
     * @throws IOException the io exception
     */
    boolean createIndex(String indexName) throws IOException;

    /**
     * Exists index boolean.
     * @param indexName the index name
     * @return the boolean
     * @throws IOException the io exception
     */
    boolean existsIndex(String indexName) throws IOException;

    /**
     * Delete index boolean.
     * @param indexName the index name
     * @return the boolean
     * @throws IOException the io exception
     */
    boolean deleteIndex(String indexName) throws IOException;

    /**
     * Add string.
     * @param index the index
     * @param json  the json
     * @return the string
     * @throws IOException the io exception
     */
    String add(String index, String json) throws IOException;

    /**
     * Bulk add.
     * @param index    the index
     * @param dataList the data list
     * @throws IOException the io exception
     */
    void bulkAdd(String index, List<Map<String, Object>> dataList) throws IOException;

    /**
     * Update by id update response.
     * @param index the index
     * @param map   the map
     * @param id    the id
     * @return the update response
     * @throws IOException the io exception
     */
    UpdateResponse updateById(String index, Map<String, Object> map, String id) throws IOException;

    /**
     * Query match list.
     * @param indexName the index name
     * @param field     the field
     * @param keyWord   the key word
     * @return the list
     * @throws IOException the io exception
     */
    List<Map<String, Object>> queryMatch(String indexName, String field, String keyWord) throws IOException;

    /**
     * Term query list.
     * @param indexName  the index name
     * @param fieldName  the field name
     * @param fieldValue the field value
     * @return the list
     * @throws IOException the io exception
     */
    List<Map<String, Object>> termQuery(String indexName, String fieldName, String fieldValue) throws IOException;

    /**
     * Terms query list.
     * @param indexName   the index name
     * @param fieldName   the field name
     * @param fieldValues the field values
     * @return the list
     * @throws IOException the io exception
     */
    List<Map<String, Object>> termsQuery(String indexName, String fieldName, List<String> fieldValues) throws IOException;

    /**
     * Range query list.
     * @param indexName the index name
     * @param fieldName the field name
     * @param from      the from
     * @param to        the to
     * @return the list
     * @throws IOException the io exception
     */
    List<Map<String, Object>> rangeQuery(String indexName, String fieldName, String from, String to) throws IOException;

    /**
     * Delete by query.
     * @param index        the index
     * @param queryBuilder the query builder
     * @throws IOException the io exception
     */
    void deleteByQuery(String index, QueryBuilder queryBuilder) throws IOException;

    /**
     * Update by query bulk by scroll response.
     * @param index        the index
     * @param queryBuilder the query builder
     * @return the bulk by scroll response
     * @throws IOException the io exception
     */
    BulkByScrollResponse updateByQuery(String index, QueryBuilder queryBuilder) throws IOException;

}
