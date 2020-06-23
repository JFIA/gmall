package com.rafel.gmall.service;

import com.rafel.gmall.bean.PmsSearchParam;
import com.rafel.gmall.bean.PmsSearchSkuInfo;

import java.io.IOException;
import java.util.List;

public interface SearchService {

    List<PmsSearchSkuInfo> getList(PmsSearchParam pmsSearchParam) throws IOException;
}
