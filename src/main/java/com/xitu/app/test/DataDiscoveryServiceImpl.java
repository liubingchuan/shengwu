package com.xitu.app.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xitu.app.service.es.ESHttpClient;


public class DataDiscoveryServiceImpl {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	static int num = 1; 
	static List<String> listhzlw = new ArrayList();


	public JSONObject searchPaper(HttpServletRequest request,String param, String author, String date,String jou,String ins,String sub,String lwlx,String uesremail,String userip, int pageNo, String source, String orderBy,
			String method) {
		// TODO Auto-generated method stub
		JSONObject conES = new JSONObject();
		List<String> plist = new ArrayList();
		if (param != null && !param.equals("") && !param.equals("null")) {
			plist.add(param);
			conES = ESHttpClient.conES(ESHttpClient.createQqueryJsonPage(param, plist, author, date, jou, ins, sub,
					lwlx, pageNo, source, orderBy, method));
			if (conES != null) {
				JSONObject hits = (JSONObject) conES.get("hits");
				JSONObject aggregations = (JSONObject) conES.get("aggregations");
				if (aggregations != null) {
					JSONObject article_year = (JSONObject) aggregations.get("article_year");
					JSONArray bucketnian = (JSONArray) article_year.get("buckets");
					List<Object> janum = new ArrayList();

					Set<String> s = new TreeSet();
					Map<String, Object> job = new LinkedHashMap<String, Object>();
					if (bucketnian != null && bucketnian != null) {
						if (bucketnian.size() > 0) {
							for (int i = 0; i < bucketnian.size(); i++) {
								JSONObject jo = (JSONObject) (bucketnian.get(i));
								String key = jo.get("key") + "";
								if (!key.equals("") && !key.equals("null") && key != null) {
									// job.put("name", key);
									s.add(key);
									job.put(key, jo.get("doc_count"));

								}
							}
						}
					}
					Iterator<String> it = s.iterator();
					while (it.hasNext()) {
						String key = it.next();
						if (job.containsKey(key)) {
							janum.add(job.get(key));
						}
					}

					conES.put("date", janum);
					conES.put("sj", s);
				}
				int count = new Integer(hits.get("total") + "");
				int pageCount = (count % 10 == 0 ? count / 10 : count / 10 + 1);
				conES.put("pageCount", pageCount);
				conES.put("pageNo", pageNo);
		
				String abs = "";
				JSONArray arrdd = (JSONArray) hits.get("hits");
				if (arrdd.size() > 0) {
					for (int i = 0; i < arrdd.size(); i++) {
						JSONObject jo = (JSONObject) (arrdd.get(i));
						JSONObject data = (JSONObject) jo.get("_source");
						abs += getPaperEsData(data, "article_abstract");
						String consource = getPaperEsData(data, "source");
						if (consource.equals("WOSNSTL_JournalArticle") || consource.equals("Elsevier_JournalArticle")
								|| consource.equals("CSCD_JournalArticle") || consource.equals("VIP_JournalArticle")
								|| consource.equals("IR_Article")) {
							String jouname = getPaperEsData(data, "source_source-title");
							String jouissn = getPaperEsData(data, "source_issn");
							if (!jouissn.equals("")) {
								if (jouissn.indexOf("-") >= 0) {
									jouissn = jouissn;
								} else {
									jouissn = jouissn.substring(0, 4) + "-" + jouissn.substring(4, 8);
								}
							}

							if (jouname.equals("")) {
								data.put("light", "yellow");
								continue;
							}
							
						}
					}
					
					conES.put("abs", abs);
				}

			}
		}
		
		
		return conES;
	}
	
	public JSONObject jiance(String q,String year,String institution,String lanmu, Integer pageIndex) {
		// TODO Auto-generated method stub
		JSONObject conES = new JSONObject();
		List<String> plist = new ArrayList();
		
			
		conES = ESHttpClient.conES(ESHttpClient.jcreateQqueryJsonPage(q, year, institution, lanmu, pageIndex));
//			if (conES != null) {
//				JSONObject hits = (JSONObject) conES.get("hits");
//				JSONObject aggregations = (JSONObject) conES.get("aggregations");
//				if (aggregations != null) {
//					JSONObject article_year = (JSONObject) aggregations.get("article_year");
//					JSONArray bucketnian = (JSONArray) article_year.get("buckets");
//					List<Object> janum = new ArrayList();
//
//					Set<String> s = new TreeSet();
//					Map<String, Object> job = new LinkedHashMap<String, Object>();
//					if (bucketnian != null && bucketnian != null) {
//						if (bucketnian.size() > 0) {
//							for (int i = 0; i < bucketnian.size(); i++) {
//								JSONObject jo = (JSONObject) (bucketnian.get(i));
//								String key = jo.get("key") + "";
//								if (!key.equals("") && !key.equals("null") && key != null) {
//									// job.put("name", key);
//									s.add(key);
//									job.put(key, jo.get("doc_count"));
//
//								}
//							}
//						}
//					}
//					Iterator<String> it = s.iterator();
//					while (it.hasNext()) {
//						String key = it.next();
//						if (job.containsKey(key)) {
//							janum.add(job.get(key));
//						}
//					}
//
//					conES.put("date", janum);
//					conES.put("sj", s);
//				}
//				int count = new Integer(hits.get("total") + "");
//				int pageCount = (count % 10 == 0 ? count / 10 : count / 10 + 1);
//				conES.put("pageCount", pageCount);
//				//conES.put("pageNo", pageNo);
//
//			}
		
		
		
		return conES;
	}
	
	 public String getClientIP(HttpServletRequest request) {
		 String ip = request.getHeader("x-forwarded-for"); 
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	           ip = request.getHeader("Proxy-Client-IP"); 
	       } 
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	           ip = request.getHeader("WL-Proxy-Client-IP"); 
	       } 
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	           ip = request.getRemoteAddr(); 
	       } 
	       return ip; 
	    }
	public String getPaperEsData(JSONObject data,String name){
		String abs = "";
		List<Object> article_abstract = (List<Object>)data.get(name);
		if(article_abstract!=null && article_abstract.size()>0){
			List<Object> abstractList = (List<Object>)article_abstract.get(0);
			List<Object> abstractLists  = null;
			if(abstractList!=null){
				abstractLists = (List<Object>)abstractList.get(0);
			}
			if(abstractLists!=null){
				abs += abstractLists.get(0).toString();
			}
		}
		return abs;
	}


	
	public JSONObject searchPaperOne(HttpServletRequest request, String value, String uuid, int pageNo,int pageNoxs) {
		JSONObject reData = new JSONObject();
		JSONObject conESxs = ESHttpClient.conES(ESHttpClient.createQqueryJsonPagePaper(value, uuid, pageNoxs));
		
		JSONObject hitsxs = (JSONObject) conESxs.get("hits");
		int countxs = new Integer(hitsxs.get("total") + "");
		int pageCountxs = (countxs % 10 == 0 ? countxs / 10 : countxs / 10 + 1);
		reData.put("pageCountxs", pageCountxs);
		reData.put("pageNoxs", pageNoxs);
		reData.put("countxs", countxs);
		reData.put("dataxs", conESxs);
		return reData;
	}
	/*public JSONObject searchJiJinAgg(HttpServletRequest request, String param, String year,String type, String ins,String author, String xmzz,String lang,int pageNo,
			String orderBy, String method) {
		JSONObject conES = null;
		try {
			
			conES = ESHttpClient
					.conESJiJin(ESHttpClient.createQqueryJiJinJsonGLAgg(param, year, type,ins,author,xmzz, lang,pageNo, orderBy, method));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		if (param != null && !param.equals("") && !param.equals("null")) {
//			conES = ESHttpClient
//					.conESJiJin(ESHttpClient.createQqueryJiJinJsonGL(param, year, type,ins,author,xmzz, pageNo, orderBy, method));
//		} else {
//			conES = ESHttpClient
//					.conESJiJin(ESHttpClient.createQqueryFundJson("a", year, type, ins,author,xmzz,pageNo, orderBy, method));
//		}
		//conES = findJJAuthorAndIns(conES);
		return conES;
	}*/

	public JSONObject findPaperBycs(String doi,String type){
		JSONObject redata = new JSONObject();
		int countcgs = 0;
		String[] arr = doi.split(",");
		JSONArray arrcgs = new JSONArray();
		for(String doiid : arr){
			if(!doiid.equals("") && doiid != ""){
				countcgs = getHtmlBypc(doiid,type);
			}else{
				countcgs = 0;
			}
			arrcgs.add(countcgs);
		}
		redata.put("countbycs", arrcgs);
		return redata;
	}

	public int getHtmlBypc(String doiid,String type){
		String result = "";
		int bycs = 0;
		try {
			// List formParams = new ArrayList();
			// formParams.add(new BasicNameValuePair("keyword", keyword));
			// formParams.add(new BasicNameValuePair("domain", domaintype));
			// HttpEntity entity = new UrlEncodedFormEntity(formParams,
			// "UTF-8");
			String url = "http://iswitch.las.ac.cn/wos/wosSearch?act=identify";
			if(type.equals("doi")){
				url += "&doi="+doiid;
			}
			if(type.equals("ut")){
				url += "&ut="+doiid;
			}
			HttpPost request = new HttpPost(url);
			HttpClient client = new DefaultHttpClient();
			result = client.execute(request, new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							return new String(EntityUtils.toByteArray(entity), "UTF-8");
						}
					}
					return "";
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (result != null && !result.equals("")) {
			// 解析结果
			try {
				Document doc = DocumentHelper.parseText(result);
				Element mapElement = doc.getRootElement().element("fn").element("map");
				Element fooElement;
				for (Iterator i = mapElement.elementIterator("map"); i.hasNext();) {
					fooElement = (Element) i.next();
					String city = fooElement.attributeValue("name");
					String citylx = "";
					if(type.equals("doi")){
						citylx = "cite_2";
					}
					if(type.equals("ut")){
						citylx = "cite_1";
					}
					if (city.equals(citylx)) {
						Element c2 = fooElement.element("map");
						for (Iterator j = c2.elementIterator("val"); j.hasNext();) {
							Element val = (Element) j.next();
							if (val.attributeValue("name").equals("timesCited")) {
								////System.out.println(val.getText());
								bycs = new Integer(val.getText());
							}
						}
					}
					// //System.out.println(""+fooElement.elementText("NO"));
				}
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		return bycs;
	}


}
