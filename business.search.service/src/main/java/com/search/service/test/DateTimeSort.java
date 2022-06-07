package com.search.service.test;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * The type Date time sort.
 */
public class DateTimeSort {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeSort.class);


    /**
     * The entry point of application.
     * @param args the input arguments
     */
    public static void main(String[] args) {
        List<SmsQuotaResult> smsQuotaResultList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            smsQuotaResultList.add(new SmsQuotaResult(DateUtils.addDays(new Date(), new Random().nextInt(10)), i + 1));
        }
        LOGGER.info("init smsQuotaResultList:{}", smsQuotaResultList);
        // 根据日期排序
        smsQuotaResultList = smsQuotaResultList.stream().sorted((SmsQuotaResult smsQuotaResult1, SmsQuotaResult smsQuotaResult2) -> (int) (smsQuotaResult1.getSendTime().getTime() - smsQuotaResult2.getSendTime().getTime())).collect(Collectors.toList());
        LOGGER.info("sorted smsQuotaResultList:{}", smsQuotaResultList);
    }
}
