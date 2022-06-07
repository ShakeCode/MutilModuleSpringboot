package com.redis.service.controller;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * https://blog.csdn.net/a495023351/article/details/102059772
 * <p>
 * 布隆过滤器（Bloom Filter）是非常经典的，以空间换时间的算法。布隆过滤器由布隆在 1970 年提出。它实际上是一个很长的二进制向量和一系列随机映射函数。布隆过滤器可以用于检索一个元素是否在一个集合中。它的优点是空间效率和查询时间都远远超过一般的算法，缺点是有一定的误识别率和删除困难。
 *
 * 1、布隆过滤器添加元素
 * 将要添加的元素给 k 个哈希函数
 * 得到对应于位数组上的 k 个位置
 * 将这 k 个位置设为 1
 * 2、布隆过滤器查询元素
 * 将要查询的元素给 k 个哈希函数
 * 得到对应于位数组上的 k 个位置
 * 如果 k 个位置有一个为 0，则肯定不在集合中
 * 如果 k 个位置全部为 1，则可能在集合中
 * 3、布隆过滤器的优缺点
 * 优点
 *
 * 相比于其它的数据结构，布隆过滤器在空间和时间方面都有巨大的优势。布隆过滤器存储空间和插入/查询时间都是常数。另外，Hash 函数相互之间没有关系，方便由硬件并行实现。布隆过滤器不需要存储元素本身，在某些对保密要求非常严格的场合有优势。
 * 布隆过滤器可以表示全集，其它任何数据结构都不能。
 * 缺点
 * 但是布隆过滤器的缺点和优点一样明显。误算率（False Positive）是其中之一。随着存入的元素数量增加，误算率随之增加（误判补救方法是：再建立一个小的白名单，存储那些可能被误判的信息）。但是如果元素数量太少，则使用散列表足矣。
 * 另外，一般情况下不能从布隆过滤器中删除元素。我们很容易想到把位列阵变成整数数组，每插入一个元素相应的计数器加 1, 这样删除元素时将计数器减掉就可以了。然而要保证安全的删除元素并非如此简单。首先我们必须保证删除的元素的确在布隆过滤器里面. 这一点单凭这个过滤器是无法保证的。另外计数器回绕也会造成问题。
 *
 * 四、应用场景
 * 利用布隆过滤器减少磁盘 IO 或者网络请求，因为一旦一个值必定不存在的话，我们可以不用进行后续昂贵的查询请求，比如以下场景：
 *
 * 1、大数据去重；
 *
 * 2、网页爬虫对 URL 的去重，避免爬取相同的 URL 地址；
 *
 * 3、反垃圾邮件，从数十亿个垃圾邮件列表中判断某邮箱是否垃圾邮箱；
 *
 * 4、缓存击穿，将已存在的缓存放到布隆中，当黑客访问不存在的缓存时迅速返回避免缓存及数据库挂掉。
 * The type Guava bloom test.
 */
public class GuavaBloomTest {

    // 数据容量
    private static final int size = 1000 * 100;

    // 误差率,越小需要的内存越多,不可以为0,始终会有误差
    private static final double fpp = 0.01;

    private static final BloomFilter<Integer> BLOOM_FILTER = BloomFilter.create(Funnels.integerFunnel(), size, fpp);

    /**
     * The entry point of application.
     * @param args the input arguments
     */
    public static void main(String[] args) {
        // 新增数据
        for (int i = 0; i < size; i++) {
            BLOOM_FILTER.put(i);
        }
        int count = 0;
        for (int i = 0; i < size; i++) {
            // 使用 mightContain 方法判断元素是否存在
            if (!BLOOM_FILTER.mightContain(i)) {
                count++;
                System.out.println(i + " Misjudgment");
            }
        }
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(2);
        System.out.printf("Total number of misjudgments:%d, 误判率:%s", count,percentFormat.format(count/size));
        System.out.println();
        testStringBloom();
    }

    public static void testStringBloom() {
        int insertions = 100000;

        BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), insertions, 0.001);

        Set<String> sets = new HashSet<>(insertions);

        List<String> lists = new ArrayList<>(insertions);

        for (int i = 0; i < insertions; i++) {
            String uid = UUID.randomUUID().toString();
            bloomFilter.put(uid);
            sets.add(uid);
            lists.add(uid);
        }

        int right = 0;
        int wrong = 0;

        for (int i = 0; i < 10000; i++) {
            String data = i % 100 == 0 ? lists.get(i / 100) : UUID.randomUUID().toString();
            if (bloomFilter.mightContain(data)) {
                if (sets.contains(data)) {
                    right++;
                    continue;
                }
                wrong++;
            }
        }

        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(2);
        float percent = (float) wrong / 9900;
        float bingo = (float) (9900 - wrong) / 9900;

        System.out.println("在 " + insertions + " 条数据中，判断 100 实际存在的元素，布隆过滤器认为存在的数量为：" + right);
        System.out.println("在 " + insertions + " 条数据中，判断 9900 实际不存在的元素，布隆过滤器误认为存在的数量为：" + wrong);
        System.out.println("命中率为：" + percentFormat.format(bingo) + "，误判率为：" + percentFormat.format(percent));
    }
}
