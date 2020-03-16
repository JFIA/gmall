package com.rafel.gmall.search.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.rafel.gmall.bean.PmsSearchParam;
import com.rafel.gmall.bean.PmsSearchSkuInfo;
import com.rafel.gmall.bean.PmsSkuAttrValue;
import com.rafel.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> getList(PmsSearchParam pmsSearchParam) {

        String dsl = getDsl(pmsSearchParam);

        // 用API执行复杂查询
        Search search = new Search.Builder(dsl).addIndex("gmall05").addType("PmsSkuInfo").build();

        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);

        List<PmsSearchSkuInfo> pmsSearchSkuInfos=new ArrayList<>();

        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;

            Map<String, List<String>> highlight = hit.highlight;

            if(highlight!=null){
                String skuName = highlight.get("skuName").get(0);
                source.setSkuName(skuName);
            }

            pmsSearchSkuInfos.add(source);
        }

        return pmsSearchSkuInfos;
    }

    public String getDsl(PmsSearchParam pmsSearchParam){

        String[] pmsSkuAttrValues = pmsSearchParam.getValueId();

        String keyword = pmsSearchParam.getKeyword();

        String catalog3Id = pmsSearchParam.getCatalog3Id();


        // jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // filter
        if (StringUtils.isNotBlank(catalog3Id)){

            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id","61");
            boolQueryBuilder.filter(termQueryBuilder);

        }

        if(pmsSkuAttrValues!=null){

            for (String pmsSkuAttrValue : pmsSkuAttrValues) {

                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",pmsSkuAttrValue);
                boolQueryBuilder.filter(termQueryBuilder);

            }
        }

        // must查询
        if (StringUtils.isNotBlank(keyword)){

            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        // query
        SearchSourceBuilder query = searchSourceBuilder.query(boolQueryBuilder);

        query.from(0);
        query.size(20);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;'>");
        highlightBuilder.field("skuName");

        query.highlight(highlightBuilder);

        return query.toString();

    }
}
