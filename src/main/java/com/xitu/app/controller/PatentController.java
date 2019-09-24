package com.xitu.app.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xitu.app.common.R;
import com.xitu.app.common.cache.CachePool;
import com.xitu.app.common.request.AgPersonRequest;
import com.xitu.app.common.request.AgTypeRequest;
import com.xitu.app.common.request.PatentPageListRequest;
import com.xitu.app.common.request.PriceAvgRequest;
import com.xitu.app.mapper.PatentMapper;
import com.xitu.app.mapper.PriceMapper;
import com.xitu.app.model.Org;
import com.xitu.app.model.Patent;
import com.xitu.app.model.PatentMysql;
import com.xitu.app.model.Price;
import com.xitu.app.repository.PatentRepository;
import com.xitu.app.service.es.JianceService;
import com.xitu.app.service.es.PatentService;
import com.xitu.app.utils.FileUtil;
import com.xitu.app.utils.JsonUtil;
import com.xitu.app.utils.DocUtil;
import com.xitu.app.utils.ImageUtil;
import com.xitu.app.utils.ThreadLocalUtil;



@CrossOrigin(origins = "*", maxAge = 3600, allowCredentials = "true")
@Controller
public class PatentController {

	private static final Logger logger = LoggerFactory.getLogger(PatentController.class);
	
	@Autowired
    private PatentRepository patentRepository;
	
	@Autowired
	private ElasticsearchTemplate esTemplate;
	
	@Autowired
    private PriceMapper priceMapper;
	
	@Autowired
	private PatentMapper patentMapper;
	
	@Autowired
	private PatentService patentService;
	
	// 代理隧道验证信息
    final static String ProxyUser = "H677S6B336VV189D";
    final static String ProxyPass = "8E65F1A7219B95AB";

    // 代理服务器
    final static String ProxyHost = "http-dyn.abuyun.com";
    final static Integer ProxyPort = 9020;

    // 设置IP切换头
    final static String ProxyHeadKey = "Proxy-Switch-Ip";
    final static String ProxyHeadVal = "yes";
	
	
	@GetMapping(value = "patent/get")
	public String getPatent(@RequestParam(required=false,value="id") String id, Model model) {
		Patent patent = new Patent();
		if(id != null) {
			patent = patentRepository.findById(id).get();
		}
		model.addAttribute("patent", patent);
		return "result-zlCon";
	}
	
//	@RequestMapping(value = "patent/list")
//	public String patents(@RequestParam(required=false,value="q") String q,
//			@RequestParam(required=false,value="year") String year,
//			@RequestParam(required=false,value="ipc") String ipc,
//			@RequestParam(required=false,value="cpc") String cpc,
//			@RequestParam(required=false,value="person") String person,
//			@RequestParam(required=false,value="creator") String creator,
//			@RequestParam(required=false,value="lawstatus") String lawstatus,
//			@RequestParam(required=false,value="country") String country,
//			@RequestParam(required=false,value="pageSize") Integer pageSize, 
//			@RequestParam(required=false, value="pageIndex") Integer pageIndex, 
//			Model model) {
//		if(pageSize == null) {
//			pageSize = 10;
//		}
//		if(pageIndex == null) {
//			pageIndex = 0;
//		}
//		long totalCount = 0L;
//		long totalPages = 0L;
//		List<Patent> patentList = new ArrayList<Patent>();
//		String view = "result-zl";
//		if(esTemplate.indexExists(Patent.class)) {
//			if(q == null) {
//				totalCount = patentRepository.count();
//				if(totalCount >0) {
//					Sort sort = new Sort(Direction.DESC, "now");
//					Pageable pageable = new PageRequest(pageIndex, pageSize,sort);
//					SearchQuery searchQuery = new NativeSearchQueryBuilder()
//							.withPageable(pageable).build();
//					Page<Patent> patentsPage = patentRepository.search(searchQuery);
//					patentList = patentsPage.getContent();
//				}
//			}else {
//				// 分页参数
//				Pageable pageable = new PageRequest(pageIndex, pageSize);
//
//				BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
//						.should(QueryBuilders.matchPhraseQuery("title", q))
//						.should(QueryBuilders.matchPhraseQuery("subject", q));
//				if(year != null) {
//					String[] years = year.split("-");
//					queryBuilder.filter(QueryBuilders.termsQuery("year", years));
//				}
//				if(ipc != null) {
//					String[] ipcs = ipc.split("-");
//					queryBuilder.filter(QueryBuilders.termsQuery("ipc", ipcs));
//				}
//				if(cpc != null) {
//					String[] cpcs = cpc.split("-");
//					queryBuilder.filter(QueryBuilders.termsQuery("cpc", cpcs));
//				}
//				if(person != null) {
//					String[] persons = person.split("-");
//					queryBuilder.filter(QueryBuilders.termsQuery("person", persons));
//				}
//				if(lawstatus != null) {
//					String[] lawstatuses = lawstatus.split("%");
//					queryBuilder.filter(QueryBuilders.termsQuery("lawstatus", lawstatuses));
//				}
//				if(creator != null) {
//					String[] creators = creator.split("-");
//					queryBuilder.filter(QueryBuilders.termsQuery("creator", creators));
//				}
//				if(country != null) {
//					String[] countries = country.split("-");
//					queryBuilder.filter(QueryBuilders.termsQuery("country", countries));
//				}
//				
//				// 分数，并自动按分排序
//				FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(queryBuilder, ScoreFunctionBuilders.weightFactorFunction(1000));
//
//				// 分数、分页
//				SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(pageable)
//						.withQuery(functionScoreQueryBuilder).build();
//
//				Page<Patent> searchPageResults = patentRepository.search(searchQuery);
//				patentList = searchPageResults.getContent();
//				totalCount = esTemplate.count(searchQuery, Patent.class);
//				
//				
//				BoolQueryBuilder queryBuilderAgg = QueryBuilders.boolQuery()
//						.should(QueryBuilders.matchQuery("title", q))
//						.should(QueryBuilders.matchQuery("subject", q));
//				FunctionScoreQueryBuilder functionScoreQueryBuilderAgg = QueryBuilders.functionScoreQuery(queryBuilderAgg, ScoreFunctionBuilders.weightFactorFunction(1000));
//				List<String> pList=new ArrayList<>();
//				SearchQuery nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
//						.withQuery(functionScoreQueryBuilderAgg)
//						.withSearchType(SearchType.QUERY_THEN_FETCH)
//						.withIndices("patent").withTypes("pt")
//						.addAggregation(AggregationBuilders.terms("agyear").field("publicyear").order(Terms.Order.count(false)).size(10))
//						.addAggregation(AggregationBuilders.terms("agipc").field("ipc").order(Terms.Order.count(false)).size(10))
//						.addAggregation(AggregationBuilders.terms("agcpc").field("cpc").order(Terms.Order.count(false)).size(10))
//						.addAggregation(AggregationBuilders.terms("agperson").field("person").order(Terms.Order.count(false)).size(10))
//						.addAggregation(AggregationBuilders.terms("agcreator").field("creator").order(Terms.Order.count(false)).size(10))
//						.addAggregation(AggregationBuilders.terms("agcountry").field("country").order(Terms.Order.count(false)).size(10))
//						.addAggregation(AggregationBuilders.terms("aglawstatus").field("lawstatus").order(Terms.Order.count(false)).size(10))
//						.build();
//				Aggregations aggregations = esTemplate.query(nativeSearchQueryBuilder, new ResultsExtractor<Aggregations>() {
//			        @Override
//			        public Aggregations extract(SearchResponse response) {
//			            return response.getAggregations();
//			        }
//			    });
//				
//				if(aggregations != null) {
//					StringTerms yearTerms = (StringTerms) aggregations.asMap().get("agyear");
//					Iterator<Bucket> yearbit = yearTerms.getBuckets().iterator();
//					Map<String, Long> yearMap = new HashMap<String, Long>();
//					while(yearbit.hasNext()) {
//						Bucket yearBucket = yearbit.next();
//						yearMap.put(yearBucket.getKey().toString(), Long.valueOf(yearBucket.getDocCount()));
//					}
//					model.addAttribute("agyear", yearMap);
//					
//					StringTerms ipcTerms = (StringTerms) aggregations.asMap().get("agipc");
//					Iterator<Bucket> ipcbit = ipcTerms.getBuckets().iterator();
//					Map<String, Long> ipcMap = new HashMap<String, Long>();
//					while(ipcbit.hasNext()) {
//						Bucket ipcBucket = ipcbit.next();
//						ipcMap.put(ipcBucket.getKey().toString(), Long.valueOf(ipcBucket.getDocCount()));
//					}
//					model.addAttribute("agipc", ipcMap);
//					
//					StringTerms lawstatusTerms = (StringTerms) aggregations.asMap().get("aglawstatus");
//					Iterator<Bucket> lawstatusbit = lawstatusTerms.getBuckets().iterator();
//					Map<String, Long> lawstatusMap = new HashMap<String, Long>();
//					while(lawstatusbit.hasNext()) {
//						Bucket lawstatusBucket = lawstatusbit.next();
//						lawstatusMap.put(lawstatusBucket.getKey().toString(), Long.valueOf(lawstatusBucket.getDocCount()));
//					}
//					model.addAttribute("aglawstatus", lawstatusMap);
//					
//					StringTerms cpcTerms = (StringTerms) aggregations.asMap().get("agcpc");
//					Iterator<Bucket> cpcbit = cpcTerms.getBuckets().iterator();
//					Map<String, Long> cpcMap = new HashMap<String, Long>();
//					while(cpcbit.hasNext()) {
//						Bucket cpcBucket = cpcbit.next();
//						cpcMap.put(cpcBucket.getKey().toString(), Long.valueOf(cpcBucket.getDocCount()));
//					}
//					
//					model.addAttribute("agcpc", cpcMap);
//					
//					StringTerms personTerms = (StringTerms) aggregations.asMap().get("agperson");
//					Iterator<Bucket> personbit = personTerms.getBuckets().iterator();
//					Map<String, Long> personMap = new HashMap<String, Long>();
//					while(personbit.hasNext()) {
//						Bucket personBucket = personbit.next();
//						personMap.put(personBucket.getKey().toString(), Long.valueOf(personBucket.getDocCount()));
//					}
//					model.addAttribute("agperson", personMap);
//					
//					StringTerms creatorTerms = (StringTerms) aggregations.asMap().get("agcreator");
//					Iterator<Bucket> creatorbit = creatorTerms.getBuckets().iterator();
//					Map<String, Long> creatorMap = new HashMap<String, Long>();
//					while(creatorbit.hasNext()) {
//						Bucket creatorBucket = creatorbit.next();
//						creatorMap.put(creatorBucket.getKey().toString(), Long.valueOf(creatorBucket.getDocCount()));
//					}
//					model.addAttribute("agcreator", creatorMap);
//					
//					StringTerms countryTerms = (StringTerms) aggregations.asMap().get("agcountry");
//					Iterator<Bucket> countrybit = countryTerms.getBuckets().iterator();
//					Map<String, Long> countryMap = new HashMap<String, Long>();
//					while(countrybit.hasNext()) {
//						Bucket countryBucket = countrybit.next();
//						countryMap.put(countryBucket.getKey().toString(), Long.valueOf(countryBucket.getDocCount()));
//					}
//					model.addAttribute("agcountry", countryMap);
//					
//				}
////				nativeSearchQueryBuilder.withQuery(functionScoreQueryBuilder);
////				nativeSearchQueryBuilder.withSearchType(SearchType.QUERY_THEN_FETCH);
////				TermsAggregationBuilder termsAggregation = AggregationBuilders.terms("aglist").field("list").order(Terms.Order.count(false)).size(10);
////				nativeSearchQueryBuilder.addAggregation(termsAggregation);
////		    	NativeSearchQuery nativeSearchQuery = nativeSearchQueryBuilder.build();
////		    	Page<Project> search = projectRepository.search(nativeSearchQuery);
////		    	List<Project> content = search.getContent();
////		    	for (Project project : content) {
////		    		pList.add(esBlog.getUsername());
////				}
//				//totalPages = Math.round(totalCount/pageSize);
//				if(totalCount % pageSize == 0){
//					totalPages = totalCount/pageSize;
//				}else{
//					totalPages = totalCount/pageSize + 1;
//				}
//				
//				
//			}
//		}
//		model.addAttribute("patentList", patentList);
//		model.addAttribute("pageSize", pageSize);
//		model.addAttribute("pageIndex", pageIndex);
//		model.addAttribute("totalPages", totalPages);
//		model.addAttribute("totalCount", totalCount);
//		model.addAttribute("query", q);
//			
//		return view;
//	}
	
	
	@GetMapping(value = "patent/update")
	public String updateOrg() {
		String fileContent = null;
		String filePath = "/Users/liubingchuan/Desktop/ren.json";
		String output = "/Users/liubingchuan/Desktop/renout.json";
		List<String> list = new LinkedList<String>();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		try {
			fileContent = FileUtils.readFileToString(new File(filePath), "utf-8");
			JSONArray array = JsonUtil.parseArray(fileContent);
			Iterator<Object> it = array.iterator();
			while (it.hasNext()) {
				JSONObject ob = (JSONObject) it.next();
				list.add(ob.getString("key"));
			}
		} catch (IOException e) {
		}
		Iterator<Patent> patents = patentRepository.findAll().iterator();
		int i=0;
		while(patents.hasNext()) {
			if (list.size() >0) {
				Patent patent = patents.next();
				List<String> creators = patent.getCreator();
				for(String c: creators) {
					if(list.contains(c)){
						map.put(c, patent.getPerson());
						list.remove(c);
					}
				}
			}else {
				break;
			}
			i++;
			System.out.println("the num is" + i);
		}
		try {
			String line = System.getProperty("line.separator");
			StringBuffer str = new StringBuffer();
			FileWriter fw = new FileWriter(output, true);
			Set set = map.entrySet();
			Iterator iter = set.iterator();
			while(iter.hasNext()){
			Map.Entry entry = (Map.Entry)iter.next();
			str.append(entry.getKey()+" : "+entry.getValue()).append(line);
			}
			fw.write(str.toString());
			fw.close();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		return "fasdf";
	}
	
	
	@RequestMapping(value = "patent/list")
	public String patents(@RequestParam(required=false,value="q") String q,
			@RequestParam(required=false,value="person") String person,
			@RequestParam(required=false,value="creator") String creator,
			@RequestParam(required=false,value="publicyear") String publicyear,
			@RequestParam(required=false,value="ipc") String ipc,
			@RequestParam(required=false,value="country") String country,
			@RequestParam(required=false,value="lawstatus") String lawstatus,
			@RequestParam(required=false,value="pageSize") Integer pageSize, 
			@RequestParam(required=false, value="pageIndex") Integer pageIndex, 
			Model model) {
		if(pageSize == null) {
			pageSize = 10;
		}
		if(pageIndex == null) {
			pageIndex = 0;
		}
		model.addAttribute("pageIndex", pageIndex);
		model.addAttribute("pageSize", pageSize);
		int i = 0;//0代表专利；1代表论文；2代表项目；3代表监测
		// TODO 静态变量未环绕需调整
		ThreadLocalUtil.set(model);
		JSONObject jObject = patentService.execute(pageIndex, pageSize, i,q,person,creator,null,publicyear,ipc,country,lawstatus);
		if (jObject != null) {
			CachePool cache = CachePool.getInstance();
			if (q == null || "".equals(q)) {
				cache.putCacheItem("total", jObject);
			}else{
				cache.putCacheItem(q, jObject);
			}
		}
		ThreadLocalUtil.remove();
		String view = "result-zl";
		return view;
	}
	
	@ResponseBody
	@RequestMapping(value = "patent/patentMonList", method = RequestMethod.POST,consumes = "application/json")
	public R patentmonthList(@RequestBody JSONObject queryobj) {
    	int pageSize = 10;
//		if(pageIndex == null) {
//		   pageIndex = 0;
//		}
    	String query= (String)queryobj.get("querystring");
    	Calendar cale = null;  
        cale = Calendar.getInstance();  
        int year = cale.get(Calendar.YEAR);  
    	
		int i = 0;//0代表专利；1代表论文；2代表项目；3代表监测;4代表机构；5代表专家；
		// TODO 静态变量未环绕需调整
		
		int[] num = patentService.executeMonth(0, 10, i,query,null,null,""+year,null,null,null,null);
		return R.ok().put("num", num).put("query", query);
    }
	
	@ResponseBody
	@RequestMapping(value = "patent/patentLastMonList", method = RequestMethod.POST,consumes = "application/json")
	public R patentlastmonthList(@RequestBody JSONObject queryobj) {
    	int pageSize = 10;
//		if(pageIndex == null) {
//		   pageIndex = 0;
//		}
    	String query= (String)queryobj.get("querystring");
    	Calendar cale = null;  
        cale = Calendar.getInstance();  
        int year = cale.get(Calendar.YEAR);  
        int month = cale.get(Calendar.MONTH)+1;  
        String[] hengzhoushuju = {month-2+"月",month-1+"月",month+"月"};
		int i = 0;//0代表专利；1代表论文；2代表项目；3代表监测;4代表机构；5代表专家；
		// TODO 静态变量未环绕需调整
		
		int[] num = patentService.executeMonth(0, 10, i,query,null,null,""+year,null,null,null,null);
		int[] b = new int[3];
		System.arraycopy(num, month-3, b, 0, 3);
		return R.ok().put("b", b).put("query", query).put("hengzhoushuju", hengzhoushuju);
    }
	
	@GetMapping(value = "patent/agtype")
	public String agtype(@RequestParam(required=false,value="q") String q,
			@RequestParam(required=false,value="totalcount") String totalCount,
			Model model) {
		CachePool cache = CachePool.getInstance();
	    JSONObject obj = new JSONObject();
	    //cache.putCacheItem("abc", obj);
	    if (q == null || "".equals(q)) {
	    	 obj = (JSONObject) cache.getCacheItem("total");
		}else{
			 obj = (JSONObject) cache.getCacheItem(q);
		}
	    Calendar cale = null;  
        cale = Calendar.getInstance();  
        int year = cale.get(Calendar.YEAR);  
        String[] str=new String[5];
        int[] famingnumtotal={0,0,0,0,0};
	    int[] famingnum={0,0,0,0,0};
	    int[] famingshouquannum={0,0,0,0,0};
	    int[] shiyongnum={0,0,0,0,0};
	    int[] waiguannum={0,0,0,0,0};
	    int lastfive = year-4;
	    for(int i=0;i<5;i++){
	    	str[i] = lastfive+"";
	    	lastfive++;
	    }
	    
	    JSONObject agg = (JSONObject) obj.get("typeyear");
	    JSONArray ar = (JSONArray) agg.get("buckets");
	    
	    for(Object jsonObject : ar){
	    	for(int j = 0;j<str.length;j++){
	    		if(((JSONObject)jsonObject).get("key").toString().contains(str[j])){
	    			if(((JSONObject)jsonObject).get("key").toString().contains("发明_")){
	    				famingnum[j] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
	    			}
	    			if(((JSONObject)jsonObject).get("key").toString().contains("发明授权_")){
	    				famingshouquannum[j] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
	    			}
	    			if(((JSONObject)jsonObject).get("key").toString().contains("实用新型_")){
	    				shiyongnum[j] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
	    			}
	    			if(((JSONObject)jsonObject).get("key").toString().contains("外观_")){
	    				waiguannum[j] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
	    			}
		    	}
	    	}
	    	
	    }
	    for(int i=0; i<5; i++){
	    	famingnumtotal[i] = famingnum[i] + famingshouquannum[i];
		}
		//model.addAttribute(key, agg.get("buckets"));
	    model.addAttribute("famingnumtotal", famingnumtotal);
	    model.addAttribute("waiguannum", waiguannum);
	    model.addAttribute("shiyongnum", shiyongnum);
	    model.addAttribute("yearstr", str);
	    if(totalCount == null || totalCount.equals("")){
	    	totalCount = 163942+"";
	    }
	    model.addAttribute("totalCount", totalCount); 
	    model.addAttribute("query", q);
		return "zhuanlifenxizhuanlileixing";
	}
	
	@ResponseBody
	@RequestMapping(value = "patent/patentTypeMonList", method = RequestMethod.POST,consumes = "application/json")
	public R patenttypemonthList(@RequestBody JSONObject queryobj) {
    	int pageSize = 10;
//		if(pageIndex == null) {
//		   pageIndex = 0;
//		}
    	String query= (String)queryobj.get("querystring");
    	Calendar cale = null;  
        cale = Calendar.getInstance();  
        int year = cale.get(Calendar.YEAR);  
    	
		int i = 0;//0代表专利；1代表论文；2代表项目；3代表监测;4代表机构；5代表专家；
		// TODO 静态变量未环绕需调整
		
		JSONObject num = patentService.executeTypeMonth(0, 10, i,query,null,null,""+year,null,null,null,null);
		return R.ok().put("famingnumtotal", num.get("famingnumtotal")).put("waiguannum", num.get("waiguannum")).put("shiyongnum", num.get("shiyongnum")).put("query", query);
    }
	
	@ResponseBody
	@RequestMapping(value = "patent/patentTypeLastMonList", method = RequestMethod.POST,consumes = "application/json")
	public R patenttypelastmonthList(@RequestBody JSONObject queryobj) {
    	int pageSize = 10;
//		if(pageIndex == null) {
//		   pageIndex = 0;
//		}
    	String query= (String)queryobj.get("querystring");
    	Calendar cale = null;  
        cale = Calendar.getInstance();  
        int year = cale.get(Calendar.YEAR);  
        int month = cale.get(Calendar.MONTH)+1;  
        String[] hengzhoushuju = {month-2+"月",month-1+"月",month+"月"};
		int i = 0;//0代表专利；1代表论文；2代表项目；3代表监测;4代表机构；5代表专家；
		// TODO 静态变量未环绕需调整
		
		JSONObject num = patentService.executeTypeMonth(0, 10, i,query,null,null,""+year,null,null,null,null);
		int[] b1 = new int[3];
		int[] b2 = new int[3];
		int[] b3 = new int[3];
		System.arraycopy(num.get("famingnumtotal"), month-3, b1, 0, 3);
		System.arraycopy(num.get("waiguannum"), month-3, b2, 0, 3);
		System.arraycopy(num.get("shiyongnum"), month-3, b3, 0, 3);
		
		return R.ok().put("famingnumtotal", b1).put("waiguannum",b2).put("shiyongnum", b3).put("query", query).put("hengzhoushuju", hengzhoushuju);
		
    }
	
	@GetMapping(value = "patent/agmount")
	public String agmount(@RequestParam(required=false,value="q") String q,
			@RequestParam(required=false,value="totalcount") String totalCount,
			Model model) {
		CachePool cache = CachePool.getInstance();
	    JSONObject obj = new JSONObject();
	    //cache.putCacheItem("abc", obj);
	    if (q == null || "".equals(q)) {
	    	 obj = (JSONObject) cache.getCacheItem("total");
		}else{
			 obj = (JSONObject) cache.getCacheItem(q);
		}
	    Calendar cale = null;  
        cale = Calendar.getInstance();  
        int year = cale.get(Calendar.YEAR);  
        String[] str=new String[5];
	    int[] num={0,0,0,0,0};
	    int lastfive = year-4;
	    for(int i=0;i<5;i++){
	    	str[i] = lastfive+"";
	    	lastfive++;
	    }
	    
	    JSONObject agg = (JSONObject) obj.get("applyyear");
	    JSONArray ar = (JSONArray) agg.get("buckets");
	    
	    for(Object jsonObject : ar){
	    	for(int j = 0;j<str.length;j++){
	    		if(((JSONObject)jsonObject).get("key").equals(str[j])){
		    		num[j] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
		    	}
	    	}
	    	
	    }
		//model.addAttribute(key, agg.get("buckets"));
	    model.addAttribute("num", num);
	    model.addAttribute("yearstr", str);
	    if(totalCount == null || totalCount.equals("")){
	    	totalCount = 163942+"";
	    }
	    model.addAttribute("totalCount", totalCount); 
	    model.addAttribute("query", q);
		return "zhuanlifenxizhuanlishenqingliang";
	}
	
	
	@ResponseBody
	@RequestMapping(value = "patent/download", method = RequestMethod.POST,consumes = "application/json")
	public R xiangguanpaperList(@RequestBody JSONObject info) {
		String barBase64Info = (String) info.get("barBase64Info");
		DocUtil docUtil = new DocUtil();
	    //引入处理图片的工具类，包含将base64编码解析为图片并保存本地，获取图片本地路径
	    ImageUtil imageUtil = new ImageUtil();
	    //建立map存储所要导出到word的各种数据和图像，不能使用自己项目封装的类型，例如PageData
	    Map<String, Object> dataMap = new HashMap<String, Object>(); 
	    
	  //这一步，进行图片的处理，获取前台传过来的图片base64编码，在利用工具类解析图片保存到本地，然后利用工具类获取图片本地地址
	   
	    String path = "C:";
	    
	    //String image1  = imageUtil.getImageStr(image1);
	    
	    
		try {
			String image1 = ImageUtil.savePictoServer(barBase64Info, path);
			image1  = imageUtil.getImageStr(image1);
			
			dataMap.put("image1", image1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    

	    File file = null;
	    InputStream fin = null;
	    OutputStream out = null;
	    String filename = "文件名.doc";
	        //dataMap是上面处理完的数据，MODELPATH是模板文件的存储路径，"模板.xml"是相应的模板文件
	    file = docUtil.createWordFile(dataMap, "model.xml");
	    System.out.print(file.getAbsolutePath());
		return null;
	}
	
	@GetMapping(value = "patent/agcountry")
	public String agcountry(@RequestParam(required=false,value="q") String q,
			@RequestParam(required=false,value="totalcount") String totalCount,
			Model model) {
		CachePool cache = CachePool.getInstance();
	    JSONObject obj = new JSONObject();
	    //cache.putCacheItem("abc", obj);
	    if (q == null || "".equals(q)) {
	    	 obj = (JSONObject) cache.getCacheItem("total");
		}else{
			 obj = (JSONObject) cache.getCacheItem(q);
		}
	   
	    int[] num={0,0,0,0,0};
	    
	    JSONObject agg = (JSONObject) obj.get("country");
	    JSONArray ar = (JSONArray) agg.get("buckets");
	    int j=0;
	    for(Object jsonObject : ar){
	    	
	    	if(((JSONObject)jsonObject).get("key").equals("中国")){
		    	num[j] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
		    }
	    	j++;
	    	
	    	
	    }
		//model.addAttribute(key, agg.get("buckets"));
	    model.addAttribute("num", num);
	   // model.addAttribute("yearstr", str);
	    model.addAttribute("query", q);
	    model.addAttribute("totalCount", totalCount); 
		return "zhuanlifenxizhuanlishenqingguo";
	}
	
	@GetMapping(value = "patent/agpeople")
	public String agpeople(@RequestParam(required=false,value="q") String q,
			@RequestParam(required=false,value="totalcount") String totalCount,
			Model model) {
		
//		int pageSize = 10;
//		int pageIndex = 0;
//		
//		//model.addAttribute("pageIndex", pageIndex);
//		//model.addAttribute("pageSize", pageSize);
//		int i = 0;//0代表专利；1代表论文；2代表项目；3代表监测
//		// TODO 静态变量未环绕需调整
//		ThreadLocalUtil.set(model);
//		patentService.executefamingren(pageIndex, pageSize, i,null,"person","creator");
//		ThreadLocalUtil.remove();
		CachePool cache = CachePool.getInstance();
	    JSONObject obj = new JSONObject();
	    //cache.putCacheItem("abc", obj);
	    if (q == null || "".equals(q)) {
	    	 obj = (JSONObject) cache.getCacheItem("total");
		}else{
			 obj = (JSONObject) cache.getCacheItem(q);
		}
        String[] str=new String[10];
	    int[] num=new int[10];
	    
	    
	    JSONObject agg = (JSONObject) obj.get("creator");
	    JSONArray ar = (JSONArray) agg.get("buckets");
	    int j = 0;
	    for(Object jsonObject : ar){
	    	
	    		str[j] = ((JSONObject)jsonObject).get("key").toString();
		    	num[j] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
		    	j++;
		    	if(j>=10){
		    		break;
		    	}
	    	
	    }
	    model.addAttribute("num", num);
	    model.addAttribute("famingren", str);
	    
	    String[] zhuanliquanrenstr=new String[10];
	    int[] zhuanliquanrennum=new int[10];
	    
	    
	    JSONObject zhuanliquanrenagg = (JSONObject) obj.get("person");
	    JSONArray zhuanliquanrenar = (JSONArray) zhuanliquanrenagg.get("buckets");
	    int jj = 0;
	    for(Object jsonObject : zhuanliquanrenar){
	    	
	    	zhuanliquanrenstr[jj] = ((JSONObject)jsonObject).get("key").toString();
	    	zhuanliquanrennum[jj] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
		    	jj++;
		    	if(jj>=10){
		    		break;
		    	}
	    	
	    }
	    model.addAttribute("zhuanliquanrennum", zhuanliquanrennum);
	    model.addAttribute("zhuanliquanrenstr", zhuanliquanrenstr);
	    
	    if(totalCount == null || totalCount.equals("")){
	    	totalCount = 163942+"";
	    }
	    model.addAttribute("totalCount", totalCount); 
	    model.addAttribute("query", q);
		return "zhuanlifenxifamingrenjizhuanliquanren";
	}
	
	@GetMapping(value = "patent/agclassis")
	public String agclassis(@RequestParam(required=false,value="q") String q,
			@RequestParam(required=false,value="totalcount") String totalCount,
			Model model) {
		CachePool cache = CachePool.getInstance();
	    JSONObject obj = new JSONObject();
	    //cache.putCacheItem("abc", obj);
	    if (q == null || "".equals(q)) {
	    	 obj = (JSONObject) cache.getCacheItem("total");
		}else{
			 obj = (JSONObject) cache.getCacheItem(q);
		}
        String[] str=new String[10];
	    int[] num=new int[10];
	    
	    
	    JSONObject agg = (JSONObject) obj.get("ipc");
	    JSONArray ar = (JSONArray) agg.get("buckets");
	    int j = 0;
	    for(Object jsonObject : ar){
	    	
	    		str[j] = ((JSONObject)jsonObject).get("key").toString();
		    	num[j] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
		    	j++;
		    	if(j>=10){
		    		break;
		    	}
	    	
	    }
	    model.addAttribute("num", num);
	    model.addAttribute("famingren", str);
	    model.addAttribute("query", q);
	    if(totalCount == null || totalCount.equals("")){
	    	totalCount = 163942+"";
	    }
	    model.addAttribute("totalCount", totalCount); 
	    model.addAttribute("query", q);
		return "zhuanlifenxijishufenlei";
	}
	
	@ResponseBody
	@RequestMapping(value = "patent/patentJishuqushi", method = RequestMethod.POST,consumes = "application/json")
	public R patentjishuqushi(@RequestBody JSONObject queryobj) {
    	int pageSize = 10;
//		if(pageIndex == null) {
//		   pageIndex = 0;
//		}
    	String q= (String)queryobj.get("querystring");
    	
    	CachePool cache = CachePool.getInstance();
	    JSONObject obj = new JSONObject();
	    //cache.putCacheItem("abc", obj);
	    if (q == null || "".equals(q)) {
	    	 obj = (JSONObject) cache.getCacheItem("total");
		}else{
			 obj = (JSONObject) cache.getCacheItem(q);
		}
	    // TODO  ipc
        String[] str=new String[7];
	    int[] num=new int[7];
	    String[] yearstr=new String[24];
	    Calendar cale = null;  
        cale = Calendar.getInstance();  
        int year = cale.get(Calendar.YEAR);  
        
	   // TODO year
	    int lastfive = year-23;
	    for(int i=0;i<24;i++){
	    	yearstr[i] = lastfive+"";
	    	lastfive++;
	    }
	    JSONObject agg = (JSONObject) obj.get("ipc");
	    JSONArray ar = (JSONArray) agg.get("buckets");
	    int j = 0;
	    for(Object jsonObject : ar){
	    	
	    		str[j] = ((JSONObject)jsonObject).get("key").toString();
		    	num[j] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
		    	j++;
		    	if(j>=7){
		    		break;
		    	}
	    	
	    }
	    Map qushinum = patentService.executeQushi(0, 10, 0,q,str); //key  ip_year
	    int[][] arr = new int[168][3];
		int i=0;
		for(int m=0;m<str.length;m++) {
			for(int k=0;k<yearstr.length;k++) {
				int[] tmp = new int[3];
				tmp[0] = m;
				tmp[1] = k;
				if(qushinum.containsKey(str[m] + "_" + yearstr[k])) {
					tmp[2] = (int) qushinum.get(str[m] + "_" + yearstr[k]);
				}else {
					tmp[2] = 0;
				}
				arr[i] = tmp;
				i++;
			}
		}
	    
//	    int[][] data ={{0,0,5},{0,1,1},{0,2,0},{0,3,0},{0,4,0},{0,5,0},{0,6,0},{0,7,0},{0,8,0},{0,9,0},{0,10,0},{0,11,2},{0,12,4},{0,13,1},{0,14,1},{0,15,3},{0,16,4},{0,17,6},{0,18,4},{0,19,4},{0,20,3},{0,21,3},{0,22,2},{0,23,5},{1,0,7},{1,1,0},{1,2,0},{1,3,0},{1,4,0},{1,5,0},{1,6,0},{1,7,0},{1,8,0},{1,9,0},{1,10,5},{1,11,2},{1,12,2},{1,13,6},{1,14,9},{1,15,11},{1,16,6},{1,17,7},{1,18,8},{1,19,12},{1,20,5},{1,21,5},{1,22,7},{1,23,2},{2,0,1},{2,1,1},{2,2,0},{2,3,0},{2,4,0},{2,5,0},{2,6,0},{2,7,0},{2,8,0},{2,9,0},{2,10,3},{2,11,2},{2,12,1},{2,13,9},{2,14,8},{2,15,10},{2,16,6},{2,17,5},{2,18,5},{2,19,5},{2,20,7},{2,21,4},{2,22,2},{2,23,4},{3,0,7},{3,1,3},{3,2,0},{3,3,0},{3,4,0},{3,5,0},{3,6,0},{3,7,0},{3,8,1},{3,9,0},{3,10,5},{3,11,4},{3,12,7},{3,13,14},{3,14,13},{3,15,12},{3,16,9},{3,17,5},{3,18,5},{3,19,10},{3,20,6},{3,21,4},{3,22,4},{3,23,1},{4,0,1},{4,1,3},{4,2,0},{4,3,0},{4,4,0},{4,5,1},{4,6,0},{4,7,0},{4,8,0},{4,9,2},{4,10,4},{4,11,4},{4,12,2},{4,13,4},{4,14,4},{4,15,14},{4,16,12},{4,17,1},{4,18,8},{4,19,5},{4,20,3},{4,21,7},{4,22,3},{4,23,0},{5,0,2},{5,1,1},{5,2,0},{5,3,3},{5,4,0},{5,5,0},{5,6,0},{5,7,0},{5,8,2},{5,9,0},{5,10,4},{5,11,1},{5,12,5},{5,13,10},{5,14,5},{5,15,7},{5,16,11},{5,17,6},{5,18,0},{5,19,5},{5,20,3},{5,21,4},{5,22,2},{5,23,0},{6,0,1},{6,1,0},{6,2,0},{6,3,0},{6,4,0},{6,5,0},{6,6,0},{6,7,0},{6,8,0},{6,9,0},{6,10,1},{6,11,0},{6,12,2},{6,13,1},{6,14,3},{6,15,4},{6,16,0},{6,17,0},{6,18,0},{6,19,0},{6,20,1},{6,21,2},{6,22,2},{6,23,6}}; 
	
		return R.ok().put("qushi", arr).put("ipc", str).put("year", yearstr);
		
    }
	
	@ResponseBody
	@RequestMapping(value = "patent/patentJishufenbu", method = RequestMethod.POST,consumes = "application/json")
	public R patentjishufenbu(@RequestBody JSONObject queryobj) {
    	int pageSize = 10;
//		if(pageIndex == null) {
//		   pageIndex = 0;
//		}
    	String q= (String)queryobj.get("querystring");
    	
    	CachePool cache = CachePool.getInstance();
	    JSONObject obj = new JSONObject();
	    //cache.putCacheItem("abc", obj);
	    if (q == null || "".equals(q)) {
	    	 obj = (JSONObject) cache.getCacheItem("total");
		}else{
			 obj = (JSONObject) cache.getCacheItem(q);
		}
	    // TODO  ipc
        String[] str=new String[7];
	    int[] num=new int[7];
	    String[] ipcstr=new String[18];
	   
	    JSONObject agg = (JSONObject) obj.get("person");
	    JSONArray ar = (JSONArray) agg.get("buckets");
	    int j = 0;
	    for(Object jsonObject : ar){
	    	
	    		str[j] = ((JSONObject)jsonObject).get("key").toString();
		    	num[j] = Integer.valueOf(((JSONObject)jsonObject).get("doc_count").toString());
		    	j++;
		    	if(j>=7){
		    		break;
		    	}
	    	
	    }
	    JSONObject aggipc = (JSONObject) obj.get("ipc");
	    JSONArray aripc = (JSONArray) aggipc.get("buckets");
	    int jj = 0;
	    for(Object jsonObject : aripc){
	    	
	    	ipcstr[jj] = ((JSONObject)jsonObject).get("key").toString();
		    
	    	jj++;
	    	if(jj>=18){
	    		break;
	    	}
	    	
	    }
	    Map qushinum = patentService.executeJishufenbu(0, 10, 0,q,str,ipcstr); //key  ip_year
	    int[][] arr = new int[126][3];
		int i=0;
		for(int m=0;m<str.length;m++) {
			for(int k=0;k<ipcstr.length;k++) {
				int[] tmp = new int[3];
				tmp[0] = m;
				tmp[1] = k;
				if(qushinum.containsKey(str[m] + "_" + ipcstr[k])) {
					tmp[2] = (int) qushinum.get(str[m] + "_" + ipcstr[k]);
				}else {
					tmp[2] = 0;
				}
				arr[i] = tmp;
				i++;
			}
		}
	    
	    //int[][] data ={{0,0,5},{0,1,1},{0,2,0},{0,3,0},{0,4,0},{0,5,0},{0,6,0},{0,7,0},{0,8,0},{0,9,0},{0,10,0},{0,11,2},{0,12,4},{0,13,1},{0,14,1},{0,15,3},{0,16,4},{0,17,6},{0,18,4},{0,19,4},{0,20,3},{0,21,3},{0,22,2},{0,23,5},{1,0,7},{1,1,0},{1,2,0},{1,3,0},{1,4,0},{1,5,0},{1,6,0},{1,7,0},{1,8,0},{1,9,0},{1,10,5},{1,11,2},{1,12,2},{1,13,6},{1,14,9},{1,15,11},{1,16,6},{1,17,7},{1,18,8},{1,19,12},{1,20,5},{1,21,5},{1,22,7},{1,23,2},{2,0,1},{2,1,1},{2,2,0},{2,3,0},{2,4,0},{2,5,0},{2,6,0},{2,7,0},{2,8,0},{2,9,0},{2,10,3},{2,11,2},{2,12,1},{2,13,9},{2,14,8},{2,15,10},{2,16,6},{2,17,5},{2,18,5},{2,19,5},{2,20,7},{2,21,4},{2,22,2},{2,23,4},{3,0,7},{3,1,3},{3,2,0},{3,3,0},{3,4,0},{3,5,0},{3,6,0},{3,7,0},{3,8,1},{3,9,0},{3,10,5},{3,11,4},{3,12,7},{3,13,14},{3,14,13},{3,15,12},{3,16,9},{3,17,5},{3,18,5},{3,19,10},{3,20,6},{3,21,4},{3,22,4},{3,23,1},{4,0,1},{4,1,3},{4,2,0},{4,3,0},{4,4,0},{4,5,1},{4,6,0},{4,7,0},{4,8,0},{4,9,2},{4,10,4},{4,11,4},{4,12,2},{4,13,4},{4,14,4},{4,15,14},{4,16,12},{4,17,1},{4,18,8},{4,19,5},{4,20,3},{4,21,7},{4,22,3},{4,23,0},{5,0,2},{5,1,1},{5,2,0},{5,3,3},{5,4,0},{5,5,0},{5,6,0},{5,7,0},{5,8,2},{5,9,0},{5,10,4},{5,11,1},{5,12,5},{5,13,10},{5,14,5},{5,15,7},{5,16,11},{5,17,6},{5,18,0},{5,19,5},{5,20,3},{5,21,4},{5,22,2},{5,23,0},{6,0,1},{6,1,0},{6,2,0},{6,3,0},{6,4,0},{6,5,0},{6,6,0},{6,7,0},{6,8,0},{6,9,0},{6,10,1},{6,11,0},{6,12,2},{6,13,1},{6,14,3},{6,15,4},{6,16,0},{6,17,0},{6,18,0},{6,19,0},{6,20,1},{6,21,2},{6,22,2},{6,23,6}}; 
	
		return R.ok().put("qushi", arr).put("ipc", str).put("year", ipcstr);
		
    }
	
	@ResponseBody
	@RequestMapping(value = "patent/pageList", method = RequestMethod.POST,consumes = "application/json")
	public R patentPageList(@RequestBody PatentPageListRequest request) {
		Integer pageSize = request.getPageSize();
		Integer pageIndex = request.getPageIndex();
		String year = request.getYear();
		String ipc = request.getIpc();
		String cpc = request.getCpc();
		String person = request.getPerson();
		String creator = request.getCreator();
		String country= request.getCountry();
		String q = request.getQ();
		if(pageSize == null) {
			pageSize = 10;
		}
		if(pageIndex == null) {
			pageIndex = 0;
		}
		long totalCount = 0L;
		long totalPages = 0L;
		List<Patent> patentList = new ArrayList<Patent>();
		if(esTemplate.indexExists(Patent.class)) {
			if(q == null) {
				totalCount = patentRepository.count();
				if(totalCount >0) {
					Sort sort = new Sort(Direction.DESC, "now");
					Pageable pageable = new PageRequest(pageIndex, pageSize,sort);
					SearchQuery searchQuery = new NativeSearchQueryBuilder()
							.withPageable(pageable).build();
					Page<Patent> patentsPage = patentRepository.search(searchQuery);
					patentList = patentsPage.getContent();
					if(totalCount % pageSize == 0){
						totalPages = totalCount/pageSize;
					}else{
						totalPages = totalCount/pageSize + 1;
					}
				}
			}else {
				// 分页参数
				Pageable pageable = new PageRequest(pageIndex, pageSize);

				BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
						.should(QueryBuilders.matchPhraseQuery("title", q))
						.should(QueryBuilders.matchPhraseQuery("subject", q));
				if(year != null) {
					String[] years = year.split("-");
					queryBuilder.filter(QueryBuilders.termsQuery("year", years));
				}
				if(ipc != null) {
					String[] ipcs = ipc.split("-");
					queryBuilder.filter(QueryBuilders.termsQuery("ipc", ipcs));
				}
				if(cpc != null) {
					String[] cpcs = cpc.split("-");
					queryBuilder.filter(QueryBuilders.termsQuery("cpc", cpcs));
				}
				if(person != null) {
					String[] persons = person.split("-");
					queryBuilder.filter(QueryBuilders.termsQuery("person", persons));
				}
				if(creator != null) {
					String[] creators = creator.split("-");
					queryBuilder.filter(QueryBuilders.termsQuery("creator", creators));
				}
				if(country != null) {
					String[] countries = country.split("-");
					queryBuilder.filter(QueryBuilders.termsQuery("country", countries));
				}
				
				// 分数，并自动按分排序
				FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(queryBuilder, ScoreFunctionBuilders.weightFactorFunction(1000));

				// 分数、分页
				SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(pageable)
						.withQuery(functionScoreQueryBuilder).build();

				Page<Patent> searchPageResults = patentRepository.search(searchQuery);
				patentList = searchPageResults.getContent();
				totalCount = esTemplate.count(searchQuery, Patent.class);
				if(totalCount % pageSize == 0){
					totalPages = totalCount/pageSize;
				}else{
					totalPages = totalCount/pageSize + 1;
				}
				
				BoolQueryBuilder queryBuilderAgg = QueryBuilders.boolQuery()
						.should(QueryBuilders.matchQuery("title", q))
						.should(QueryBuilders.matchQuery("subject", q));
				FunctionScoreQueryBuilder functionScoreQueryBuilderAgg = QueryBuilders.functionScoreQuery(queryBuilderAgg, ScoreFunctionBuilders.weightFactorFunction(1000));
				List<String> pList=new ArrayList<>();
				SearchQuery nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
						.withQuery(functionScoreQueryBuilderAgg)
						.withSearchType(SearchType.QUERY_THEN_FETCH)
						.withIndices("patent").withTypes("pt")
						.addAggregation(AggregationBuilders.terms("agyear").field("publicyear").order(Terms.Order.count(false)).size(10))
						.addAggregation(AggregationBuilders.terms("agipc").field("ipc").order(Terms.Order.count(false)).size(10))
						.addAggregation(AggregationBuilders.terms("agcpc").field("cpc").order(Terms.Order.count(false)).size(10))
						.addAggregation(AggregationBuilders.terms("agperson").field("person").order(Terms.Order.count(false)).size(10))
						.addAggregation(AggregationBuilders.terms("agcreator").field("creator").order(Terms.Order.count(false)).size(10))
						.addAggregation(AggregationBuilders.terms("agcountry").field("country").order(Terms.Order.count(false)).size(10))
						.build();
				Aggregations aggregations = esTemplate.query(nativeSearchQueryBuilder, new ResultsExtractor<Aggregations>() {
			        @Override
			        public Aggregations extract(SearchResponse response) {
			            return response.getAggregations();
			        }
			    });
				
				if(aggregations != null) {
					StringTerms yearTerms = (StringTerms) aggregations.asMap().get("agyear");
					Iterator<Bucket> yearbit = yearTerms.getBuckets().iterator();
					Map<String, Long> yearMap = new HashMap<String, Long>();
					while(yearbit.hasNext()) {
						Bucket yearBucket = yearbit.next();
						yearMap.put(yearBucket.getKey().toString(), Long.valueOf(yearBucket.getDocCount()));
					}
//					model.addAttribute("agyear", yearMap);
					
					StringTerms ipcTerms = (StringTerms) aggregations.asMap().get("agipc");
					Iterator<Bucket> ipcbit = ipcTerms.getBuckets().iterator();
					Map<String, Long> ipcMap = new HashMap<String, Long>();
					while(ipcbit.hasNext()) {
						Bucket ipcBucket = ipcbit.next();
						ipcMap.put(ipcBucket.getKey().toString(), Long.valueOf(ipcBucket.getDocCount()));
					}
//					model.addAttribute("agipc", ipcMap);
					
					StringTerms cpcTerms = (StringTerms) aggregations.asMap().get("agcpc");
					Iterator<Bucket> cpcbit = cpcTerms.getBuckets().iterator();
					Map<String, Long> cpcMap = new HashMap<String, Long>();
					while(cpcbit.hasNext()) {
						Bucket cpcBucket = cpcbit.next();
						cpcMap.put(cpcBucket.getKey().toString(), Long.valueOf(cpcBucket.getDocCount()));
					}
					
//					model.addAttribute("agcpc", cpcMap);
					
					StringTerms personTerms = (StringTerms) aggregations.asMap().get("agperson");
					Iterator<Bucket> personbit = personTerms.getBuckets().iterator();
					Map<String, Long> personMap = new HashMap<String, Long>();
					while(personbit.hasNext()) {
						Bucket personBucket = personbit.next();
						personMap.put(personBucket.getKey().toString(), Long.valueOf(personBucket.getDocCount()));
					}
//					model.addAttribute("agperson", personMap);
					
					StringTerms creatorTerms = (StringTerms) aggregations.asMap().get("agcreator");
					Iterator<Bucket> creatorbit = creatorTerms.getBuckets().iterator();
					Map<String, Long> creatorMap = new HashMap<String, Long>();
					while(creatorbit.hasNext()) {
						Bucket creatorBucket = creatorbit.next();
						creatorMap.put(creatorBucket.getKey().toString(), Long.valueOf(creatorBucket.getDocCount()));
					}
//					model.addAttribute("agcreator", creatorMap);
					
					StringTerms countryTerms = (StringTerms) aggregations.asMap().get("agcountry");
					Iterator<Bucket> countrybit = countryTerms.getBuckets().iterator();
					Map<String, Long> countryMap = new HashMap<String, Long>();
					while(countrybit.hasNext()) {
						Bucket countryBucket = countrybit.next();
						countryMap.put(countryBucket.getKey().toString(), Long.valueOf(countryBucket.getDocCount()));
					}
//					model.addAttribute("agcountry", countryMap);
					
				}
//				nativeSearchQueryBuilder.withQuery(functionScoreQueryBuilder);
//				nativeSearchQueryBuilder.withSearchType(SearchType.QUERY_THEN_FETCH);
//				TermsAggregationBuilder termsAggregation = AggregationBuilders.terms("aglist").field("list").order(Terms.Order.count(false)).size(10);
//				nativeSearchQueryBuilder.addAggregation(termsAggregation);
//		    	NativeSearchQuery nativeSearchQuery = nativeSearchQueryBuilder.build();
//		    	Page<Project> search = projectRepository.search(nativeSearchQuery);
//		    	List<Project> content = search.getContent();
//		    	for (Project project : content) {
//		    		pList.add(esBlog.getUsername());
//				}
				totalPages = Math.round(totalCount/pageSize);
				
				
			}
		}
//		model.addAttribute("patentList", patentList);
//		model.addAttribute("pageSize", pageSize);
//		model.addAttribute("pageIndex", pageIndex);
//		model.addAttribute("totalPages", totalPages);
//		model.addAttribute("totalCount", totalCount);
//		model.addAttribute("query", q);
			
		return R.ok().put("patentList", patentList).put("pageIndex", pageIndex);
	}
	
	
	
	@GetMapping(value = "patent/analysis")
	public String analysis() {
		return "zhuanlifenxifamingrenjizhuanliquanren";
	}
	
	
	
	@ResponseBody
	@RequestMapping(value = "patent/agpersons", method = RequestMethod.POST,consumes = "application/json")
	public R persons(@RequestBody AgPersonRequest request) {
		List<List<Object>> personList = new ArrayList<List<Object>>();
//		String view = "zhuanlifenxifamingrenjizhuanliquanren";
		String person = request.getPerson();
		String creator = request.getCreator();
		if(esTemplate.indexExists(Patent.class)) {
				
			MatchAllQueryBuilder queryBuilderAgg = QueryBuilders.matchAllQuery();
			FunctionScoreQueryBuilder functionScoreQueryBuilderAgg = QueryBuilders.functionScoreQuery(queryBuilderAgg, ScoreFunctionBuilders.weightFactorFunction(1000));
			NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
					.withQuery(functionScoreQueryBuilderAgg)
					.withSearchType(SearchType.QUERY_THEN_FETCH)
					.withIndices("patent").withTypes("pt");
			if(person != null) {
				nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("agperson").field("person").order(Terms.Order.count(true)).size(10));
			}
			if(creator != null) {
				nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("agcreator").field("creator").order(Terms.Order.count(true)).size(10));
			}
			Aggregations aggregations = esTemplate.query(nativeSearchQueryBuilder.build(), new ResultsExtractor<Aggregations>() {
				@Override
				public Aggregations extract(SearchResponse response) {
					return response.getAggregations();
				}
			});
			
			if(aggregations != null) {
				if(person != null) {
					StringTerms personTerms = (StringTerms) aggregations.asMap().get("agperson");
					Iterator<Bucket> personbit = personTerms.getBuckets().iterator();
					List<Object> titleList = new ArrayList<Object>();
					titleList.add("score");
					titleList.add("amount");
					titleList.add("product");
					personList.add(titleList);
					while(personbit.hasNext()) {
						List<Object> list = new ArrayList<Object>();
						Bucket personBucket = personbit.next();
						list.add((int)(Math.random()*90)+10);
						list.add(personBucket.getDocCount());
						list.add(personBucket.getKey().toString());
						personList.add(list);
					}
				}
				
				if(creator != null) {
					StringTerms creatorTerms = (StringTerms) aggregations.asMap().get("agcreator");
					Iterator<Bucket> creatorbit = creatorTerms.getBuckets().iterator();
					while(creatorbit.hasNext()) {
						List<Object> list = new ArrayList<Object>();
						Bucket creatorBucket = creatorbit.next();
						list.add((int)(Math.random()*90)+10);
						list.add(creatorBucket.getDocCount());
						list.add(creatorBucket.getKey().toString());
						personList.add(list);
					}
				}
			}
		}
		
		return R.ok().put("agpersons", personList);
	}
	
	
	
//	@GetMapping(value = "patent/agmount")
//	public String agmount(Model model) {
//		String time = priceMapper.getLatestUpdateTime();
//		if(time != null) {
//			List<Price> prices = priceMapper.getPricesByUpdateTime(time);
//			model.addAttribute("prices", prices);
//		}else {
//			model.addAttribute("prices", new ArrayList<String>());
//		}
//		List<String> items = priceMapper.getPricesGroupByName();
//		model.addAttribute("items", items);
//		return "zhuanlifenxizhuanlishenqingliang";
//	}
	
	@GetMapping(value = "patent/agpeoplecon")
	public String agpeoplecon() {
		return "T-hangyeCon";
	}
	
	
	@GetMapping(value = "patent/agclassiscon")
	public String agclassiscon() {
		return "T-jigouCon";
	}
	
	@GetMapping(value = "patent/agtypecon")
	public String agtypecon() {
		return "T-rencaiCon";
	}
	
	@GetMapping(value = "price")
	public String price() {
		

		for(int i=4;i>0;i--) {
			String url="http://nm.sci99.com/news/?page=" + i + "&sid=8784&siteid=10" ;
			String base = "http://nm.sci99.com";
			try {
	        	
//	        	Price exist = priceMapper.getPriceByUpdateTime(ymd);
	        	
//	        	if(exist != null) {
//	        		return;
//	        	}

	            Document doc = Jsoup.connect(url).get();

	            Elements module = doc.getElementsByClass("ul_w690");

	            Document moduleDoc = Jsoup.parse(module.toString());

	            Elements lis = moduleDoc.getElementsByTag("li");  //选择器的形式

	            Map< String, String> urls= new TreeMap<String, String>();
	            for (Element li : lis){
	                Document liDoc = Jsoup.parse(li.toString());
	                Elements hrefs = liDoc.select("a[href]");
	                for(Element elem: hrefs) {
	                	System.out.println(elem.text().substring(elem.text().length()-9,elem.text().length()-1));
	                	
	                	if(!"".equals(elem.attr("href"))){
	                		String href = elem.attr("href");
	                		urls.put(base + href, elem.text().substring(elem.text().length()-9,elem.text().length()-1));
	                	}
	                }

	            }
	            for(Map.Entry<String, String> entry: urls.entrySet()) {
	            	
	            	Document singleDoc = Jsoup.connect(entry.getKey()).get();
//	            if(!singleDoc.toString().contains(ymd)){
//	            	return;
//	            }
	            	Element zoom = singleDoc.getElementById("zoom");
	            	Elements trElements = zoom.select("tr");
	            	boolean ignore = true;
	            	for(Element tdelement : trElements) {
	            		if(ignore) {
	            			ignore = false;
	            			continue;
	            		}
	            		Elements tdes = tdelement.select("td");
	            		Price price = new Price();
	            		price.setUpdateTime(entry.getValue());
	            		price.setName(tdes.get(0).text());
	            		price.setDescription(tdes.get(1).text());
	            		price.setUnit(tdes.get(6).text());
	            		price.setPrice(tdes.get(3).text());
	            		price.setAvg(tdes.get(4).text());
	            		Price yesterday = priceMapper.getLatestPrice(price.getName());
	            		if(yesterday == null) {
	            			price.setFloating("100%");
	            		}else {
	            			float before = Float.valueOf(yesterday.getAvg());
	            			float now = Float.valueOf(tdes.get(4).text());
	            			float delta = now - before;
	            			if(delta != 0) {
	            				System.out.println();
	            			}
	            			NumberFormat numberFormat = NumberFormat.getInstance();
	            			numberFormat.setMaximumFractionDigits(2);
	            			String result = numberFormat.format(delta / before * 100);
	            			price.setFloating(result + "%");
	            		}
	            		priceMapper.insertPrice(price);
	            		System.out.println("插入成功" + price.getName());
	            	}
	            	//  String title = clearfixli.getElementsByTag("a").text();
	            	System.out.println("fasdf");
	            }


	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "T-rencaiCon";
	}
	
	@ResponseBody
	@RequestMapping(value = "price/avg", method = RequestMethod.POST,consumes = "application/json")
	public R priceAvg(@RequestBody PriceAvgRequest request) {
		String time = request.getTime();
		String name = request.getName();
		Map<String, String> map = new HashMap<String, String>();
		String date = "";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		if("3".equals(time)) {
			for(int i=0;i<3;i++) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.MONTH, -i);
				Date lastmonth = c.getTime();
				date = formatter.format(lastmonth);
				String start = date.substring(0, 6) + "00";
				String end = date.substring(0,6) + "32";
				String avg = priceMapper.getAvgPricesGroupByName(start, end , name);
				if(avg == null) {
					avg = "0";
				}else if(avg.contains(".")) {
					avg = avg.split("\\.")[0];
				}
				map.put(date.substring(4, 6), avg);
				
			}
		}
		return R.ok().put("avg", map);
	}
	
	@ResponseBody
	@RequestMapping(value = "patent/agtypes", method = RequestMethod.POST,consumes = "application/json")
	public R types(@RequestBody AgTypeRequest request) {
		List<List<Object>> typeList = new ArrayList<List<Object>>();
//		String view = "zhuanlifenxifamingrenjizhuanliquanren";
		String type = request.getType();
		String trend = request.getTrend();
		if(esTemplate.indexExists(Patent.class)) {
				
			MatchAllQueryBuilder queryBuilderAgg = QueryBuilders.matchAllQuery();
			FunctionScoreQueryBuilder functionScoreQueryBuilderAgg = QueryBuilders.functionScoreQuery(queryBuilderAgg, ScoreFunctionBuilders.weightFactorFunction(1000));
			NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
					.withQuery(functionScoreQueryBuilderAgg)
					.withSearchType(SearchType.QUERY_THEN_FETCH)
					.withIndices("patent").withTypes("pt");
			if(type != null) {
				nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("agipc").field("ipc").order(Terms.Order.count(true)).size(10));
			}
			if(trend != null) {
				nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("agipc").field("ipc").order(Terms.Order.count(true)).size(10).subAggregation(AggregationBuilders.terms("agyear").field("year").order(Terms.Order.count(true)).size(10)));
			}
			Aggregations aggregations = esTemplate.query(nativeSearchQueryBuilder.build(), new ResultsExtractor<Aggregations>() {
				@Override
				public Aggregations extract(SearchResponse response) {
					return response.getAggregations();
				}
			});
			
			if(aggregations != null) {
				if(type != null) {
					StringTerms ipcTerms = (StringTerms) aggregations.asMap().get("agipc");
					Iterator<Bucket> ipcbit = ipcTerms.getBuckets().iterator();
					List<Object> titleList = new ArrayList<Object>();
					titleList.add("score");
					titleList.add("amount");
					titleList.add("product");
					typeList.add(titleList);
					while(ipcbit.hasNext()) {
						List<Object> list = new ArrayList<Object>();
						Bucket ipcBucket = ipcbit.next();
						list.add((int)(Math.random()*90)+10);
						list.add(ipcBucket.getDocCount());
						list.add(ipcBucket.getKey().toString());
						typeList.add(list);
					}
				}
				
				if(trend != null) {
//					StringTerms creatorTerms = (StringTerms) aggregations.asMap().get("agcreator");
//					Iterator<Bucket> creatorbit = creatorTerms.getBuckets().iterator();
//					while(creatorbit.hasNext()) {
//						List<Object> list = new ArrayList<Object>();
//						Bucket creatorBucket = creatorbit.next();
//						list.add((int)(Math.random()*90)+10);
//						list.add(creatorBucket.getDocCount());
//						list.add(creatorBucket.getKey().toString());
//						personList.add(list);
//					}
				}
			}
		}
		
		return R.ok().put("agpersons", typeList);
	}
	
	
//	@ResponseBody
//	@RequestMapping(value = "patent/fetch/local", method = RequestMethod.GET)
//	public R fetchLocal(@RequestParam(required=false,value="interval") Integer interval,
//			@RequestParam(required=false,value="patentIndex") Integer patentIndex,
//			@RequestParam(required=false,value="tail") Integer tail) {
//		String[] url={"http://www.soopat.com/Home/Result","http://www2.soopat.com/Home/Result","http://www1.soopat.com/Home/Result"};
//		String[] base = {"http://www.soopat.com","http://www1.soopat.com","http://www2.soopat.com"};
////		List<String> ipList = new ArrayList<String>();
//		List<String> missedList = new ArrayList<String>();
//		Random random = new Random();
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("SearchWord", "稀土");
//		map.put("FMZL", "Y");
//		map.put("SYXX", "Y");
//		map.put("WGZL", "Y");
//		map.put("FMSQ", "Y");
//		List<Patent> patents = new LinkedList<Patent>();
//		int out=0;
//		for(;patentIndex<tail;){
//			boolean retry = true;
//			System.out.println("开启新的一页------>patentIndex---->" + patentIndex );
//			
//			Map<String,String> innerPathMap = new HashMap<String,String>();
//			if(missedList.size() > 0) {
//				for(String s: missedList) {
//					String[] str = s.split("%");
//					innerPathMap.put(str[0], str[1]);
//				}
//				missedList.clear();
//			}
//			map.put("PatentIndex", String.valueOf(patentIndex));
//			patentIndex += 10;
//			
//			try {
//				String pageUrl = url[out%3];
//				System.out.println("pageUrl is ->" + pageUrl);
//				Connection conn = Jsoup.connect(pageUrl);
//				conn.data(map);
//				Document doc = conn.get();
//				if(doc.toString().contains("请按图片上的要求依次点击图片上对应的字符")) {
//					System.out.println("已被拦截，当前PatentIndex为"+ (patentIndex-10) + "手动干预后放开断点，并继续执行");
////					System.out.println("当前被封ip--》" + System.getProperties().getProperty("http.proxyHost") + System.getProperties().getProperty("http.proxyPort"));
//					System.out.println("正在尝试的url是 " + url[out%3]);
////					do{
////						ipIndex = random.nextInt(length);
////						ip = ipList.get(ipIndex);
////						r = ip.split(":");
////					}while(r[0].equals(System.getProperties().getProperty("http.proxyHost")));
////					System.getProperties().setProperty("http.proxyHost", r[0]);
////					System.getProperties().setProperty("http.proxyPort", r[1]);
//					// retry
//					doc = conn.get();
//
//				}
//				retry = false;
//				out++;
////				System.out.println(doc.toString());
//				Elements patentBlocks = doc.getElementsByClass("PatentBlock");
//				
//				if(patentBlocks.size()>0) {
//					for(Element patentBlock: patentBlocks) {
//						Document patentDoc = Jsoup.parse(patentBlock.toString());
//						Elements patentTypeElements = patentDoc.getElementsByClass("PatentTypeBlock");
//						if(patentTypeElements.size() == 0) {
//							continue;
//						}
//						jump:
//							for(Element pte: patentTypeElements) {
//								System.out.println(pte.text());
//								String type = extractMessageByRegular(pte.text()).get(0);
//								Document pteDoc = Jsoup.parse(pte.toString());
//								Elements hrefs = pteDoc.select("a[href]");
//								for(Element elem: hrefs) {
//									if(!"".equals(elem.attr("href"))){
//										String href = elem.attr("href");
//										innerPathMap.put(href, type);
//										break jump;
//									}
//								}
//							}
//					}
//				}else {
//					patentIndex -= 10;
//					retry = true;
//				}
//				
//				
//				int max=5000;
//				int min=2000;
//				
//				int in=0;
//				for(Map.Entry<String, String> entry: innerPathMap.entrySet()) {
//					int sleep = random.nextInt(max)%(max-min+1) + min;
//					System.out.println("休眠" + sleep + "毫秒");
//					try {
//						Thread.sleep(sleep);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					Patent patent = new Patent();
//					PatentMysql patentMysql = new PatentMysql();
//					String singleUrl = base[in%3] + entry.getKey();
//					in++;
//					
//					System.out.println("正在调用-------------------------->" + entry.getKey());
//					Connection singlePageConn = Jsoup.connect(singleUrl);
//					try {
//						Document singleDoc = singlePageConn.get();
//						System.out.println("成功调用-------------------->" + entry.getKey());
//						if(singleDoc.toString().contains("请按图片上的要求依次点击图片上对应的字符")) {
//							java.awt.Toolkit.getDefaultToolkit().beep();
//							System.out.println("已被拦截，当前PatentIndex为"+ (retry==true?patentIndex:(patentIndex-10)) + "手动干预后放开断点，并继续执行");
//							System.out.println("正在尝试的url是 " + singleUrl);
//							
//							// retry
//							singleDoc = singlePageConn.get();
//							
//
//						}
//						Elements h1Elements = singleDoc.getElementsByTag("h1");
//						if(h1Elements==null || h1Elements.size()==0) {
//							missedList.add(entry.getKey() + "%" + entry.getValue());
//							continue;
//						}
//						for(Element h1Element: h1Elements) {
//							String title = h1Element.text();
//							if(title != null) {
//								patent.setType(entry.getValue());
//								patent.setTitle(title.split(" ")[0]);
//								patent.setLawstatus(title.split(" ")[1]);
//								patentMysql.setPtype(entry.getValue());
//								patentMysql.setTitle(title.split(" ")[0]);
//								patentMysql.setLawstatus(title.split(" ")[1]);
//								break;
//							}
//						}
//						
//						Elements grayElements = singleDoc.getElementsByClass("gray");
//						for(Element grayElement: grayElements) {
//							String appliance = grayElement.text();
//							if(appliance != null) {
//								String[] s = appliance.split(" ");
//								if(s.length>=2) {
//									patent.setApplynumber(s[0].substring(4, s[0].length()));
////									patent.setId(s[0].substring(4, s[0].length()));
//									patentMysql.setApplynumber(s[0].substring(4, s[0].length()));
//									String applytime = s[1].substring(4, s[1].length());
//									patent.setApplytime(applytime);
//									patentMysql.setApplytime(applytime);
//									patent.setApplyyear(applytime.substring(0, 4));
//									patentMysql.setApplyyear(applytime.substring(0, 4));
//									break;
//								}
//							}
//						}
//						
//						Elements datainfoElements = singleDoc.getElementsByClass("datainfo");
//						for(Element dataInfo : datainfoElements) {
//							Elements tdelements = dataInfo.getElementsByTag("td");
//							for(Element td: tdelements) {
//								if(td.text().contains("摘要：")){
//									patent.setSubject(td.text());
//									patentMysql.setSubject(td.text());
//								}else if(td.text().contains("申请人：")){
//									List<String> persons = new ArrayList<String>();
//									persons.add(td.text().replace("申请人：", ""));
//									patentMysql.setPerson(td.text().replace("申请人：", ""));
//									patent.setPerson(persons);
//								}else if(td.text().contains("发明(设计)人：")) {
//									List<String> creators = new ArrayList<String>();
//									creators.add(td.text().replace("发明(设计)人：", ""));
//									patentMysql.setCreator(td.text().replace("发明(设计)人：", ""));
//									patent.setCreator(creators);
//								}else if(td.text().contains("分类号：") && (!td.text().contains("主分类号："))) {
//									List<String> ipcs = new ArrayList<String>();
//									String ipc = td.text().replace("分类号：", "");
//									String[] ipcArray = ipc.split(" ");
//									for(String s: ipcArray) {
//										ipcs.add(s);
//									}
//									patentMysql.setIpc(ipc.replace(" ", ";"));
//									patent.setIpc(ipcs);
//								}
//								System.out.println(td.text());
//							}
//						}
//						Elements vipcomElements = singleDoc.getElementsByClass("vipcom");
//						for(Element vipcom: vipcomElements) {
//							String s = vipcom.toString();
//							if(s.contains("其他信息")) {
//								int i = 0;
//								Elements tdelements = vipcom.getElementsByTag("td");
//								for(Element td: tdelements) {
//									if(i==1) {
//										patent.setClaim(td.text());
//										patentMysql.setClaim(td.text());
//									}else if(i==4) {
//										patent.setPublicnumber(td.text());
//										patentMysql.setPublicnumber(td.text());
//									}else if(i==7) {
//										String publictime = td.text().replace("&nbsp;", "").trim();
//										patent.setPublictime(publictime);
//										patentMysql.setPublictime(publictime);
//										if(publictime.contains("-")) {
//											patent.setPublicyear(publictime.split("-")[0]);
//											patentMysql.setPublicyear(publictime.split("-")[0]);
//										}
//									}else if(i==19) {
//										System.out.println("priority-----" + td.text());
//										patent.setPiroryear(td.text().equals("&nbsp;")?"":td.text());
//										patentMysql.setPiroryear(td.text().equals("&nbsp;")?"":td.text());
//									}
//									i++;
//								}
//							}
//						}
//						patent.setId(UUID.randomUUID().toString());
//						patent.setCountry("中国");
//						patent.setNow(System.currentTimeMillis());
//						patentMysql.setCountry("中国");
//						patentMysql.setNow(System.currentTimeMillis());
//						System.out.println("开始插入mysql");
//						patentMapper.insertPatent(patentMysql);
//						System.out.println("结束插入mysql");
//						System.out.println("开始插入es");
//						patentRepository.save(patent);
//						System.out.println("结束插入es");
//						patents.add(patent);
//					} catch(Exception e) {
//						e.printStackTrace();
//						System.out.println("出现异常，进入补偿队列的是---->" + entry.getKey() + "%" + entry.getValue());
//						missedList.add(entry.getKey() + "%" + entry.getValue());
//					}
//				}
//				
//				System.out.println("fasdf");
//				
//				try {
//					Thread.sleep(interval);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//		} catch (Exception e) {
//			e.printStackTrace();
//			if(retry) {
//				patentIndex -= 10;
//			}
//		}
//	}
//		System.out.println("这一轮全部结束");
//		return R.ok();
//	}
	
	@ResponseBody
	@RequestMapping(value = "patent/fetch/proxy/local", method = RequestMethod.GET)
	public R fetchProxyLocal(@RequestParam(required=false,value="interval") Integer interval,
			@RequestParam(required=false,value="patentIndex") Integer patentIndex,
			@RequestParam(required=false,value="tail") Integer tail) {
		String[] url={"http://www.soopat.com/Home/Result","http://www2.soopat.com/Home/Result","http://www1.soopat.com/Home/Result"};
		String[] base = {"http://www.soopat.com","http://www1.soopat.com","http://www2.soopat.com"};
		List<String> missedList = new ArrayList<String>();
		Random random = new Random();
		Map<String, String> map = new HashMap<String, String>();
		int month = 69;
		while(month<=450) {
//			if(month==10) {
//				System.out.println();
//			}
			System.out.println("已经到了"+month);
			String date = getLastMonth(month);
//			map.put("SearchWord", "(ZY:( 生物医药 ) OR MC:( 生物医药 ) OR SMS:(生物医药)) AND GKRQ:(" + date + ")");
			map.put("SearchWord", "(ZY:( 医疗器械 ) OR MC:( 医疗器械 ) OR SMS:(医疗器械) OR QLYQ (医疗器械)) AND GKRQ:( " + date + " )");
//		map.put("SearchWord", "稀土");
			map.put("FMZL", "Y");
			map.put("SYXX", "Y");
			map.put("WGZL", "Y");
			map.put("FMSQ", "Y");
			List<Patent> patents = new LinkedList<Patent>();
			Authenticator.setDefault(new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(ProxyUser, ProxyPass.toCharArray());
				}
			});
			
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ProxyHost, ProxyPort));
			int out=0;
			boolean cleaning = false;
			patentIndex = 10;
			tail = 20;
			for(;patentIndex<tail;){
				System.out.println("正在执行的date---------->" + date);
				if(patents.size() >= 100) {
					patentRepository.saveAll(patents);
					patents.clear();
				}
				// TODO
				boolean retry = true;
				System.out.println("开启新的一页------>patentIndex---->" + patentIndex );
				
				Map<String,String> innerPathMap = new HashMap<String,String>();
				if(missedList.size() > 0) {
					for(String s: missedList) {
						String[] str = s.split("%");
						innerPathMap.put(str[0], str[1]);
					}
					missedList.clear();
				}
				map.put("PatentIndex", String.valueOf(patentIndex));
				patentIndex += 10;
				
				try {
					
					// 看是否是在清扫
					if(!cleaning) {
						
						out++;
						String pageUrl = url[out%3];
						System.out.println("pageUrl is ->" + pageUrl);
						Connection conn = Jsoup.connect(pageUrl);
						conn.data(map);
						Document doc = conn.timeout(5000).header(ProxyHeadKey, ProxyHeadVal).proxy(proxy).get();
//					Document doc = conn.get();
//					System.out.println(doc.toString());
						if(doc.toString().contains("请按图片上的要求依次点击图片上对应的字符")) {
							System.out.println("已被拦截，当前PatentIndex为"+ (patentIndex-10) + "手动干预后放开断点，并继续执行");
//					System.out.println("当前被封ip--》" + System.getProperties().getProperty("http.proxyHost") + System.getProperties().getProperty("http.proxyPort"));
							System.out.println("正在尝试的url是 " + url[out%3]);
							doc = conn.timeout(5000).header(ProxyHeadKey, ProxyHeadVal).proxy(proxy).get();
//						doc = conn.get();
							
						}
						
						retry = false;
//				System.out.println(doc.toString());
						Elements patentBlocks = doc.getElementsByClass("PatentBlock");
						if(doc.toString().contains("没有搜索到相关专利，原因可能是")) {
							System.out.println(date + "没有搜索到相关专利");
							break;
						}
						if(patentBlocks.size()>0) {
							Elements right = doc.getElementsByClass("right");
//							System.out.println(right.text());
							s:
								for(Element e: right) {
									Elements ele = e.getElementsByTag("b");
									for(Element elet: ele) {
										if(!"".equals(elet.text())) {
											tail = Integer.valueOf(elet.text());
											if(tail > 1000) {
												tail = 1000;
											}
										}
										break s;
									}
//									System.out.println(e.text());
								}
							for(Element patentBlock: patentBlocks) {
								Document patentDoc = Jsoup.parse(patentBlock.toString());
								Elements patentTypeElements = patentDoc.getElementsByClass("PatentTypeBlock");
								if(patentTypeElements.size() == 0) {
									continue;
								}
								jump:
									for(Element pte: patentTypeElements) {
										System.out.println(pte.text());
										String type = extractMessageByRegular(pte.text()).get(0);
										Document pteDoc = Jsoup.parse(pte.toString());
										Elements hrefs = pteDoc.select("a[href]");
										for(Element elem: hrefs) {
											if(!"".equals(elem.attr("href"))){
												String href = elem.attr("href");
												innerPathMap.put(href, type);
												break jump;
											}
										}
									}
							}
						}else {
							patentIndex -= 10;
							retry = true;
						}
					}
					
					
//				int max=5000;
//				int min=2000;
					
					int in=0;
					for(Map.Entry<String, String> entry: innerPathMap.entrySet()) {
//					int sleep = random.nextInt(max)%(max-min+1) + min;
//					System.out.println("休眠" + sleep + "毫秒");
//					try {
//						Thread.sleep(sleep);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
						Patent patent = new Patent();
						PatentMysql patentMysql = new PatentMysql();
						String singleUrl = base[in%3] + entry.getKey();
						in++;
						
						System.out.println("正在调用-------------------------->" + entry.getKey());
						Connection singlePageConn = Jsoup.connect(singleUrl);
						try {
							System.out.println("正在执行的date---------->" + date);
							Document singleDoc = singlePageConn.timeout(3000).header(ProxyHeadKey, ProxyHeadVal).proxy(proxy).get();
//						Document singleDoc = singlePageConn.get();
							System.out.println("成功调用-------------------->" + entry.getKey());
							System.out.println("******************" + singleDoc.toString());
							System.out.println("当前的month是--------------->" + month);
							if(singleDoc.toString().contains("请按图片上的要求依次点击图片上对应的字符")) {
								java.awt.Toolkit.getDefaultToolkit().beep();
								System.out.println("已被拦截，当前PatentIndex为"+ (retry==true?patentIndex:(patentIndex-10)) + "手动干预后放开断点，并继续执行");
								System.out.println("正在尝试的url是 " + singleUrl);
								
								// retry
								singleDoc = singlePageConn.timeout(3000).header(ProxyHeadKey, ProxyHeadVal).proxy(proxy).get();
//							singleDoc = singlePageConn.get();
								
								
							}
							System.out.println("******** step1");
							Elements h1Elements = singleDoc.getElementsByTag("h1");
							if(h1Elements==null || h1Elements.size()==0) {
								missedList.add(entry.getKey() + "%" + entry.getValue());
								System.out.println("未获取block，进入补偿队列的是---->" + entry.getKey() + "%" + entry.getValue());
								continue;
							}
							System.out.println("********** step2");
							for(Element h1Element: h1Elements) {
								String title = h1Element.text();
								if(title != null) {
									patent.setType(entry.getValue());
									patent.setTitle(title.split(" ")[0]);
									String lawStatus = title.split(" ")[1];
									if("".equals(lawStatus)) {
										System.out.println("法律状态异常");
										patent.setLawstatus("未知");
									}
									System.out.println(lawStatus);
									patent.setLawstatus(title.split(" ")[1]);
									patentMysql.setPtype(entry.getValue());
									patentMysql.setTitle(title.split(" ")[0]);
									patentMysql.setLawstatus(title.split(" ")[1]);
									break;
								}
							}
							System.out.println("正在执行的date---------->" + date);
							System.out.println("************ step3");
							Elements grayElements = singleDoc.getElementsByClass("gray");
							for(Element grayElement: grayElements) {
								String appliance = grayElement.text();
								if(appliance != null) {
									String[] s = appliance.split(" ");
									if(s.length>=2) {
										patent.setApplynumber(s[0].substring(4, s[0].length()));
//									patent.setId(s[0].substring(4, s[0].length()));
										patentMysql.setApplynumber(s[0].substring(4, s[0].length()));
										String applytime = s[1].substring(4, s[1].length());
										patent.setApplytime(applytime);
										patentMysql.setApplytime(applytime);
										patent.setApplyyear(applytime.substring(0, 4));
										patentMysql.setApplyyear(applytime.substring(0, 4));
										break;
									}
								}
							}
							
							Elements datainfoElements = singleDoc.getElementsByClass("datainfo");
							for(Element dataInfo : datainfoElements) {
								Elements tdelements = dataInfo.getElementsByTag("td");
								for(Element td: tdelements) {
									if(td.text().contains("摘要：")){
										patent.setSubject(td.text());
										patentMysql.setSubject(td.text());
									}else if(td.text().contains("申请人：")){
										List<String> persons = new ArrayList<String>();
										String personstr = td.text().replace("申请人：", "").trim();
										String[] personArray = personstr.split(" ");
										for(String s: personArray) {
											persons.add(s);
										}
										patentMysql.setPerson(personstr.replace(" ", ";"));
										patent.setPerson(persons);
									}else if(td.text().contains("发明(设计)人：")) {
										List<String> creators = new ArrayList<String>();
										String creatorstr = td.text().replace("发明(设计)人：", "").trim();
										String[] creatorArray = creatorstr.split(" ");
										for(String s: creatorArray) {
											creators.add(s);
										}
										patentMysql.setCreator(creatorstr.replace(" ", ";"));
										patent.setCreator(creators);
									}else if(td.text().contains("分类号：") && (!td.text().contains("主分类号："))) {
										List<String> ipcs = new ArrayList<String>();
										String ipc = td.text().replace("分类号：", "").trim();
										String[] ipcArray = ipc.split(" ");
										for(String s: ipcArray) {
											ipcs.add(s);
										}
										patentMysql.setIpc(ipc.replace(" ", ";"));
										patent.setIpc(ipcs);
									}
									System.out.println(td.text());
								}
							}
							Elements vipcomElements = singleDoc.getElementsByClass("vipcom");
							for(Element vipcom: vipcomElements) {
								String s = vipcom.toString();
								if(s.contains("其他信息")) {
									int i = 0;
									Elements tdelements = vipcom.getElementsByTag("td");
									for(Element td: tdelements) {
										if(i==1) {
											String claim = td.text().length()>2000?td.text().substring(0,2000):td.text();
											patent.setClaim(claim);
											patentMysql.setClaim(td.text());
										}else if(i==4) {
											patent.setPublicnumber(td.text());
											patentMysql.setPublicnumber(td.text());
										}else if(i==7) {
											String publictime = td.text().replace("&nbsp;", "").trim();
											patent.setPublictime(publictime);
											patentMysql.setPublictime(publictime);
											if(publictime.contains("-")) {
												patent.setPublicyear(publictime.split("-")[0]);
												patentMysql.setPublicyear(publictime.split("-")[0]);
											}
										}else if(i==19) {
											System.out.println("priority-----" + td.text());
											patent.setPiroryear(td.text().equals("&nbsp;")?"":td.text());
											patentMysql.setPiroryear(td.text().equals("&nbsp;")?"":td.text());
										}
										i++;
									}
								}
							}
							System.out.println("****************last step");
							patent.setId(UUID.randomUUID().toString());
							patent.setCountry("中国");
							patent.setNow(System.currentTimeMillis());
							patentMysql.setCountry("中国");
//						patentMysql.setNow(System.currentTimeMillis());
							System.out.println("开始插入mysql");
							patentMapper.insertPatent(patentMysql);
							System.out.println("结束插入mysql");
							System.out.println("开始插入es");
//							patentRepository.save(patent);
							System.out.println("结束插入es");
							patents.add(patent);
						} catch(Exception e) {
							e.printStackTrace();
							System.out.println("出现异常，进入补偿队列的是---->" + entry.getKey() + "%" + entry.getValue());
							missedList.add(entry.getKey() + "%" + entry.getValue());
						}
					}
					
					System.out.println("fasdf");
					
					try {
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("当前month是：" + month);
					System.out.println("当前pageindex是：" +patentIndex);
					if(retry) {
						patentIndex -= 10;
					}
					// 将补偿队列还原回来
					for(Map.Entry<String, String> entry: innerPathMap.entrySet()) {
						missedList.add(entry.getKey() + "%" + entry.getValue());
					}
					if(patents.size()>10) {
						patentRepository.saveAll(patents);
						patents.clear();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				// 末轮清空补偿队列
				if(patentIndex >= tail && (!missedList.isEmpty())) {
					patentIndex -= 10;
					cleaning = true;
				}
			}
			if(patents.size() > 0) {
				patentRepository.saveAll(patents);
			}
			System.out.println(date + "这一轮全部结束");
			month++;
		}
		return R.ok();
	}
	
	
	private String getLastMonth(int month){
        Calendar curr = Calendar.getInstance(); 
        curr.set(Calendar.MONTH,curr.get(Calendar.MONTH)-month); //减少一月
        Date date=curr.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");  
        String dateNowStr = sdf.format(date);  
        return dateNowStr;
    }
	
	private List<String> extractMessageByRegular(String msg){
		
		List<String> list=new ArrayList<String>();
		Pattern p = Pattern.compile("(\\[[^\\]]*\\])");
		Matcher m = p.matcher(msg);
		while(m.find()){
			list.add(m.group().substring(1, m.group().length()-1));
		}
		return list;
	}
	
	@ResponseBody
	@RequestMapping(value = "patent/patentInsList", method = RequestMethod.POST,consumes = "application/json")
	public R expertInsList(@RequestBody JSONObject insname) {
    	int pageSize = 10;
//		if(pageIndex == null) {
//		   pageIndex = 0;
//		}
    	int pageIndex = (int) insname.get("pageIndex");
		int i = 5;//0代表专利；1代表论文；2代表项目；3代表监测;4代表机构；5代表专家；
		// TODO 静态变量未环绕需调整
		JSONObject rs = new JSONObject();
		rs = patentService.executeIns(insname.getString("insname"),pageIndex, pageSize, "person",i);
		return R.ok().put("list", rs.get("list")).put("totalPages", rs.get("totalPages")).put("totalCount", rs.get("totalCount")).put("pageIndex", pageIndex);
    }
	
	@ResponseBody
	@RequestMapping(value = "patent/patentExpList", method = RequestMethod.POST,consumes = "application/json")
	public R expertpatentList(@RequestBody JSONObject insname) {
    	int pageSize = 10;
//		if(pageIndex == null) {
//		   pageIndex = 0;
//		}
    	int pageIndex = (int) insname.get("pageIndex");
		int i = 5;//0代表专利；1代表论文；2代表项目；3代表监测;4代表机构；5代表专家；
		// TODO 静态变量未环绕需调整
		JSONObject rs = new JSONObject();
		rs = patentService.executeIns(insname.getString("insname"),pageIndex, pageSize, "creator",i);
		return R.ok().put("list", rs.get("list")).put("totalPages", rs.get("totalPages")).put("totalCount", rs.get("totalCount")).put("pageIndex", pageIndex);
    }
	
	public static void main(String[] args) {
		String[] p = new String[2];
		p[0] = "p1";
		p[1] = "p2";
		String[] s =  new String[2];
		s[0] = "1990";
		s[1] = "1991";
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("p1-1990", 3);
		map.put("p2-1990", 4);
		map.put("p1-1991", 5);
		map.put("p2-1991", 6);
		
		
		int[][] arr = new int[4][3];
		int i=0;
		for(int j=0;j<p.length;j++) {
			for(int k=0;k<s.length;k++) {
				int[] tmp = new int[3];
				tmp[0] = j;
				tmp[1] = k;
				tmp[2] = map.get(p[j] + "-" + s[k]);
				arr[i] = tmp;
				i++;
			}
		}
		for(int l=0;l<arr.length;l++) {
			for(int z=0;z<arr[l].length;z++) {
				System.out.print(arr[l][z]);
				System.out.print(",");
			}
			System.out.println();
		}
		
	}
	
}
