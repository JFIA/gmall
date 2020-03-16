package com.rafel.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.rafel.gmall.bean.PmsSearchSkuInfo;
import com.rafel.gmall.bean.PmsSkuInfo;
import com.rafel.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Reference
    SkuService skuService;// 查询mysql

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {

//        // jest的dsl工具
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        // bool
//        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
//
//        // filter
//        TermQueryBuilder termQueryBuilder = new TermQueryBuilder();
//        boolQueryBuilder.filter(termQueryBuilder);
//        // must
//        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder();
//        boolQueryBuilder.must(matchQueryBuilder);
//        // query
//        SearchSourceBuilder query = searchSourceBuilder.query(boolQueryBuilder);
//
//        // 用API执行复杂查询
//        Search search = new Search.Builder(query.toString()).addIndex("gmall05").addType("PmsSkuInfo").build();
//
//        SearchResult execute = jestClient.execute(search);
//        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
//
//        List<PmsSearchSkuInfo> pmsSearchSkuInfos=new ArrayList<>();
//
//        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
//            PmsSearchSkuInfo source = hit.source;
//
//            pmsSearchSkuInfos.add(source);
//        }


    }

    public void put() throws IOException {

        // 查询mysql数据

        List<PmsSkuInfo> pmsSkuInfos = skuService.getAllSkuInfo();
        // 转化为es的数据
        ArrayList<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();

            BeanUtils.copyProperties(pmsSkuInfo, pmsSearchSkuInfo);
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }

        // 导入es
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            Index input = new Index.Builder(pmsSearchSkuInfo).index("gmall05").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(input);

        }
    }

}
