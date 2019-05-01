package com.xitu.app.test;

import javax.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSONObject;


@Transactional
public interface DataDiscoveryService {
	
	public JSONObject searchPaper(HttpServletRequest request,String param,int pageNo,String source);
	public JSONObject searchPaperOne(HttpServletRequest request,String uuid);

}
