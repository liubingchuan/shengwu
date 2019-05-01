package com.xitu.app.service.es;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xitu.app.test.http.Request;
/**
 * 创建索引
 *
 * @author dell
 */

public class ESHttpClient implements Serializable {
    /**
     * 是否开启debug模式，debug模式下过程语句将会输出
     */
    public static boolean debug = false;
    /**
     * 日志对象
     */
    //private static Logger logger = Logger.getLogger(ESHttpClient.class);
    /**
     * 建索引的url
     */
    private String indexUrl;
    private String deleteUrl;
    /**
     * 查询索引的url
     */
    private String queryUrl;
    /**
     * 查询的返回值
     */
    private JSONObject queryJsonResult = null;
    /**
     * 编码
     */
    private String charset = "utf-8";

    /**
     * http访问对象
     */
    private Request request;
	protected static int nums;

    /**
     * 构造函数
     *
     * @param ip:port
     * @param
     */
    public ESHttpClient(String ipAndport, String indexName, String typeName) {

        if (indexName == null) {
            //System.out.println("indexName must not be null");
            return;
        }
        //构造查询indexurl
        this.indexUrl = "http://" + ipAndport;
        if (indexName != null)
            this.indexUrl = this.indexUrl + "/" + indexName;

        if (typeName != null)
            this.indexUrl = this.indexUrl + "/" + typeName;

        this.indexUrl = this.indexUrl + "/_bulk";

        //构造查询url
        this.queryUrl = "http://" + ipAndport;
        if (indexName != null)
            this.queryUrl = this.queryUrl + "/" + indexName;

        if (typeName != null)
            this.queryUrl = this.queryUrl + "/" + typeName;
        //preference=_primary_first&
        this.queryUrl = this.queryUrl + "/_search?search_type=dfs_query_then_fetch";

        //构造查询deleteurl
        this.deleteUrl = "http://" + ipAndport;
        if (indexName != null)
            this.deleteUrl = this.deleteUrl + "/" + indexName;

        if (typeName != null)
            this.deleteUrl = this.deleteUrl + "/" + typeName;

        this.deleteUrl = this.deleteUrl + "/_query";

        request = new Request(charset);
    }

    public ESHttpClient(String ipAndport, String indexName) {

        if (indexName == null) {
            ////System.out.println("indexName must not be null");
        }
        //构造查询indexurl
        this.indexUrl = "http://" + ipAndport;
        if (indexName != null)
            this.indexUrl = this.indexUrl + "/" + indexName;

        //this.indexUrl = this.indexUrl + "/_bulk";

        //构造查询deleteurl
        this.deleteUrl = "http://" + ipAndport;
        if (indexName != null)
            this.deleteUrl = this.deleteUrl + "/" + indexName;

        //this.deleteUrl = this.deleteUrl + "/_query";

        request = new Request(charset);
    }

    public ESHttpClient(String ipAndport) {


        //构造查询indexurl
        this.indexUrl = "http://" + ipAndport;

        //构造查询deleteurl
        this.deleteUrl = "http://" + ipAndport;

        request = new Request(charset);
    }


    /**
     * 提交请求
     *
     * @param fields 索引查询后要返回值的字段，只有建索引时，有存储的字段此处才可能有返回值，对于只索引不存储的字段，此处得不到返回值
     * @return
     */
    public JSONObject execute(String queryStr) {

        //String queryStr = "{\"query\":{\"term\":{\"art_id\":\"11284091\"}}}";
        String queryResult = request.postRequest(this.queryUrl, queryStr, charset);

        System.out.println("query: curl " + this.queryUrl + " -d " + queryStr);
        //System.out.println(queryResult);

        if (queryResult != null)
            this.queryJsonResult = JSONObject.parseObject(queryResult);

        return this.queryJsonResult;
    }

    /**
     * 返回检索结果，返回的检索字段以及字段顺序由{@link #execute(String[])} 方法中的参数fields指定
     *
     * @return 检索的结果列表
     */
    public List<String> getResults() {
        List<String> list = new LinkedList<String>();
        if (this.queryJsonResult == null || this.queryJsonResult.size() == 0 || !this.queryJsonResult.containsKey("hits"))
            return list;

        JSONArray hitJsons = this.queryJsonResult.getJSONObject("hits").getJSONArray("hits");
        for (int index = 0; index < hitJsons.size(); index++) {

            JSONObject hitJson = hitJsons.getJSONObject(index);
            ////System.out.println(hitJson.toString());
            list.add(hitJson.toString());
        }

        return list;
    }

    /**
     * 索引数据
     *
     * @param dataList      数据列表
     * @param uniqueKeyName 数据在索引中的主键名
     */
    public boolean index(List<JSONObject> dataList, String uniqueKeyName) {

        boolean rt = true;

        StringBuffer indexStrBuffer = new StringBuffer();

        if (dataList == null || dataList.size() == 0)
            return rt;

        JSONObject indexJson = new JSONObject();
        for (JSONObject dataJson : dataList) {
            //System.out.println(dataJson.toJSONString());
            //构造插入语句
            if (dataJson.containsKey(uniqueKeyName)) {
                //构造插入语句
                if (dataJson.containsKey(uniqueKeyName)) {
                    indexJson.put("_id", dataJson.get(uniqueKeyName));
                }
                indexStrBuffer.append("{\"index\":").append(indexJson.toString()).append("}\n");
                indexStrBuffer.append(dataJson.toString()).append("\n");

                indexJson.clear();

            }
        }

        String queryResultStr = request.postRequest(this.indexUrl, indexStrBuffer.toString(), charset);
        //System.out.println("index=" + queryResultStr);
        if (queryResultStr != null) {
            rt = true;
        } else {
            rt = false;
        }

        return rt;
    }

    public boolean index(String typeName, List<JSONObject> dataList, String uniqueKeyName) {
        /**
         *  构建索引url
         */
        String indexUrl = this.indexUrl;
        if (typeName != null)
            indexUrl = indexUrl + "/" + typeName;
        indexUrl = indexUrl + "/_bulk";

        boolean rt = true;

        StringBuffer indexStrBuffer = new StringBuffer();

        if (dataList == null || dataList.size() == 0)
            return rt;

        JSONObject indexJson = new JSONObject();
        for (JSONObject dataJson : dataList) {
            //构造插入语句
            if (dataJson.containsKey(uniqueKeyName)) {
                //构造插入语句
                if (dataJson.containsKey(uniqueKeyName)) {
                    indexJson.put("_id", dataJson.get(uniqueKeyName));
                }
                indexStrBuffer.append("{\"index\":").append(indexJson.toString()).append("}\n");
                indexStrBuffer.append(dataJson.toString()).append("\n");

                indexJson.clear();

            }
        }

        ////System.out.println(indexStrBuffer.toString());
        //System.out.println("indexUrl=" + indexUrl);
        //System.out.println("indexUrl json=" + indexStrBuffer.toString());
        String queryResultStr = request.postRequest(indexUrl, indexStrBuffer.toString(), charset);

        if (queryResultStr != null) {
            rt = true;
        } else {
            rt = false;
        }

        return rt;
    }


    public boolean update(String indexName, String typeName, List<JSONObject> dataList, String uniqueKeyName) {
        /**
         *  构建索引url
         */
        String indexUrl = this.indexUrl;
        if (typeName != null)
            indexUrl = indexUrl + "/" + indexName + "/" + typeName;
        indexUrl = indexUrl + "/_bulk";

        boolean rt = true;

        StringBuffer indexStrBuffer = new StringBuffer();

        if (dataList == null || dataList.size() == 0)
            return rt;

        JSONObject indexJson = new JSONObject();
        for (JSONObject dataJson : dataList) {
            //构造插入语句
            if (dataJson.containsKey(uniqueKeyName)) {
                //构造插入语句
                if (dataJson.containsKey(uniqueKeyName)) {
                    indexJson.put("_id", dataJson.get(uniqueKeyName));
                }
                indexStrBuffer.append("{\"index\":").append(indexJson.toString()).append("}\n");
                indexStrBuffer.append(dataJson.toString()).append("\n");

                indexJson.clear();
            }
        }

        //System.out.println("indexUrl=" + indexUrl);
        //System.out.println("indexUrl json=" + indexStrBuffer.toString());
        String queryResultStr = request.postRequest(indexUrl, indexStrBuffer.toString(), charset);

        if (queryResultStr != null) {
            rt = true;
        } else {
            rt = false;
        }

        return rt;
    }


    /**
     * 获取搜索结果总长度
     *
     * @return 检索结果的总长度
     */
    public long getTotal() {

        if (this.queryJsonResult == null || this.queryJsonResult.size() == 0 || !this.queryJsonResult.containsKey("hits"))
            return 0;

        return this.queryJsonResult.getJSONObject("hits").getInteger("total");

    }

    
    //生成查询的json
    public static String createQqueryJson(String value){
    	JSONObject query = new JSONObject();
//    	JSONObject match = new JSONObject();
    	JSONObject param = new JSONObject();
//    	value = "[A tutorial";
//    	param.put("article_article-title-en_ik", value);
//    	match.put("match", param);
    	JSONObject multi_match = new JSONObject();
    	String[] arr = new String[]{"article_abstract-en_ik",
    	        "article_abstract_ik",
    	        "contrib_full-name_ik",
    	        "article_article-title_ik"};
    	
//    	value = "人工智能";
    	param.put("query", value);
    	param.put("fields", arr);
    	multi_match.put("multi_match", param);
    	query.put("query", multi_match);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static JSONObject conES(String conn){
//    	ESHttpClient indexer = new ESHttpClient("10.3.11.23:9201", "t_ods_las_nstl_value_journalarticle_v5","t_ods_las_nstl_value");
    	ESHttpClient indexer = new ESHttpClient("45.77.86.209:9200", "jiance","jc");
    	//ESHttpClient indexer = new ESHttpClient("10.3.11.6:9201", "t_ods_las_nstl_value","t_ods_las_nstl_value");
        ESHttpClient.debug = true;
        JSONObject json = indexer.execute(conn);
        return json;
    }

    public static String createQqueryByUuid(String value){
    	JSONObject query = new JSONObject();
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
//    	value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	param.put("uuid", value);
    	term.put("term", param);
    	query.put("query", term);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByIdZiXun(String value){
    	JSONObject query = new JSONObject();
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
//    	value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	param.put("uuid", value);
    	term.put("term", param);
    	query.put("query", term);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
 
    public static String createQqueryByUuid(List<String> list,String type){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	
    	if(type.equals("paper")){
    		JSONArray sort = new JSONArray();
    		JSONObject uuids = new JSONObject();
    		JSONObject order1s = new JSONObject();
    		order1s.put("order", "desc");
    		uuids.put("article_year",order1s);
    		sort.add(uuids);
    		query.put("sort",sort);
    	}
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByUuid(List<String> list){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	

    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByUuidfactor(List<String> list){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", "desc");
    	_score.put("source_impact_factor",order);
    	sort.add(_score);
    	query.put("sort",sort);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByUuid(List<String> list,int pageSize){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	
    	query.put("from", 0);
    	query.put("size", pageSize);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByUuidPage(List<String> list,int pageNo){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	
    	query.put("from", pageNo);
    	query.put("size", 10);
    	
    	JSONArray sort = new JSONArray();
    	JSONObject uuids = new JSONObject();
    	JSONObject order1s = new JSONObject();
    	order1s.put("order", "desc");
    	uuids.put("article_year",order1s);
    	sort.add(uuids);
    	query.put("sort",sort);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByUuidZtpx(List<String> list,int pageSize,String ztpx){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	
    	query.put("from", 0);
    	query.put("size", pageSize);
    	

    	JSONArray sort = new JSONArray();
    	JSONObject uuids = new JSONObject();
    	JSONObject order1s = new JSONObject();
    	order1s.put("order", "desc");
    	uuids.put(ztpx,order1s);
    	sort.add(uuids);
    	query.put("sort",sort);
    	
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByUuid(List<String> list,String fenMian1,String fenMian2,int size){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", fenMian1);
    	t1.put("size", size);
    	t1.put("execution_hint", "map");
    	t1.put("collect_mode", "breadth_first");
    	p1.put("terms", t1);
    	
    	args.put(fenMian1, p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", fenMian2);
    	t2.put("size", size);
    	t2.put("execution_hint", "map");
    	t2.put("collect_mode", "breadth_first");
    	p2.put("terms", t2);
    	args.put(fenMian2, p2);
    	query.put("aggs",args);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByInsNamefromPaper(List<String> list,String fenMian1,int size){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONArray mustarr = new JSONArray();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool4 = new JSONObject();
    	JSONArray should = new JSONArray();
//		JSONObject term = new JSONObject();
//    	JSONObject param = new JSONObject();
//    	param.put("contrib_institution", insname);
//    	term.put("match", param);
//    	should.add(term);
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool4.put("bool", bool2);
    	mustarr.add(bool4);
    	
    	JSONObject range = new JSONObject();
		JSONObject datet = new JSONObject();
		JSONObject fromto = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		String currentyear = sdf.format(new Date());
		int starttime = new Integer(currentyear) - 9;
		fromto.put("from", starttime+"");
		fromto.put("to", currentyear);
		
		datet.put("article_year", fromto);  
		range.put("range", datet);
    	mustarr.add(range);
    	
    	
    	bool3.put("must", mustarr);
    	bool1.put("bool", bool3);
    	query.put("query", bool1);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", fenMian1);
    	t1.put("size", size);
    	p1.put("terms", t1);
    	args.put(fenMian1, p1);
    	query.put("aggs",args);
    	
    	query.put("from",0);
    	query.put("size", 10);
    	//System.out.println(query.toString());
    	return query.toString();
    }
    public static String createQueryUuidByPaper(List<String> list,int pageNo,String year){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONArray mustArr = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	mustArr.add(bool3);
    	if (!year.equals("") && year != null && !year.equals("null") && !year.equals("0")) {
	    	JSONObject term = new JSONObject();
	    	JSONObject param = new JSONObject();
	    	param.put("article_year",year);
	    	term.put("match", param);
	    	mustArr.add(term);
    	}
    	must.put("must",mustArr);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    
    
    public static String createQqueryByListUuid(List<String> list,String femMian,String subFM){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", femMian);
    	t1.put("size", 6);
    	p1.put("terms", t1);
    	
    	JSONObject subagg = new JSONObject();
    	JSONObject messages = new JSONObject();
    	JSONObject filters1 = new JSONObject();
    	JSONObject filters = new JSONObject();
    	JSONObject subterm = new JSONObject();
    	
    	JSONObject term1 = new JSONObject();
    	JSONObject nian1 = new JSONObject();
    	nian1.put(subFM, "2013");
    	term1.put("term", nian1);
    	subterm.put("2013", term1);
    	
    	JSONObject term2 = new JSONObject();
    	JSONObject nian2 = new JSONObject();
    	nian2.put(subFM, "2014");
    	term2.put("term", nian2);
    	subterm.put("2014", term2);
    	
    	JSONObject term3 = new JSONObject();
    	JSONObject nian3 = new JSONObject();
    	nian3.put(subFM, "2015");
    	term3.put("term", nian3);
    	subterm.put("2015", term3);
    	
    	JSONObject term4 = new JSONObject();
    	JSONObject nian4 = new JSONObject();
    	nian4.put(subFM, "2016");
    	term4.put("term", nian4);
    	subterm.put("2016", term4);
    	
    	JSONObject term5 = new JSONObject();
    	JSONObject nian5 = new JSONObject();
    	nian5.put(subFM, "2017");
    	term5.put("term", nian5);
    	subterm.put("2017", term5);
    	
    	filters1.put("filters", subterm);
    	filters.put("filters", filters1);
    	messages.put("messages", filters);
    	
    	p1.put("aggs", messages);
    	
    	args.put(femMian, p1);
    	
    	query.put("aggs",args);
    	
    	//System.out.println("*****"+query.toString());
    	return query.toString();
    	
    }
    
    
    public static String createQqueryByUuid(List<String> list,int pageNo,String uuid){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		if(!list.get(i).equals(uuid)){
    			JSONObject term = new JSONObject();
            	JSONObject param = new JSONObject();
            	param.put("uuid", list.get(i));
            	term.put("match", param);
            	should.add(term);
    		}
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByUuid(Set<String> list){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONArray should = new JSONArray();
    	Iterator<String> iterator = list.iterator();
    	while(iterator.hasNext()){
    		String next = iterator.next();
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("uuid", next);
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(String value){
    	JSONObject query = new JSONObject();
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
//    	value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	param.put("relations_o", value);
    	term.put("term", param);
    	query.put("query", term);
//    	query.put("size", 100);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByInsName(String value){
    	
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray should = new JSONArray();
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("contrib_institution", value);
    	term.put("match", param);
    	should.add(term);
    	
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("contrib_institution-superior", value);
    	term1.put("match", param1);
    	should.add(term1);
    	
    	bool2.put("should", should);
    	bool3.put("bool", bool2);
    	must.put("must",bool3);
    	bool1.put("bool", must);
    	query.put("query", bool1);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(String value,int pageNo,String rela_p){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_o", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	JSONArray sort = new JSONArray();
		JSONObject uuids = new JSONObject();
		JSONObject order1s = new JSONObject();
		order1s.put("order", "desc");
		uuids.put("uuid",order1s);
		sort.add(uuids);
		query.put("sort",sort);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    
    public static String createQqueryByRelations_o(String value,String isAll,int pageNo,String rela_p){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_o", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	
    	if(isAll.equals("no")){
    		JSONObject range = new JSONObject();
    		JSONObject datet = new JSONObject();
    		JSONObject fromto = new JSONObject();
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    		String currentyear = sdf.format(new Date());
    		//获取七天前的日期
    		Calendar calendar = Calendar.getInstance();  
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 7);  
            Date today = calendar.getTime();  
            String qiHou = sdf.format(today);  
    		fromto.put("from", qiHou+"");
    		fromto.put("to", currentyear);
    		
    		datet.put("date", fromto);  
    		range.put("range", datet);
    		must1.add(range);
    	}
    	
    	
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(List<String> list,int pageNo,String rela_p){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONArray mustarr = new JSONArray();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool4 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("relations_o", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool4.put("bool", bool2);
    	mustarr.add(bool4);
    	
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	mustarr.add(term1);
    	
    	
    	bool3.put("must", mustarr);
    	
    	
    	bool1.put("bool", bool3);
    	query.put("query", bool1);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_s(List<String> list,int pageNo,String rela_p){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONArray mustarr = new JSONArray();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool4 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("relations_s", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	bool4.put("bool", bool2);
    	mustarr.add(bool4);
    	
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	mustarr.add(term1);
    	
    	
    	bool3.put("must", mustarr);
    	
    	
    	bool1.put("bool", bool3);
    	query.put("query", bool1);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(List<String> list,String rela_p){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONArray mustarr = new JSONArray();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool4 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("relations_o", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	
//    	JSONObject term1 = new JSONObject();
//    	JSONObject param1 = new JSONObject();
//    	param1.put("relations_p", rela_p);
//    	term1.put("match", param1);
//    	bool2.put("must_not",term1);
    	
    	bool4.put("bool", bool2);
    	mustarr.add(bool4);
    	
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	mustarr.add(term1);
    	
//    	JSONObject term2 = new JSONObject();
//    	JSONObject param2 = new JSONObject();
//    	param2.put("relations_p", "contribute_institution");
//    	term2.put("match", param2);
//    	mustarr.add(term2);
    	
    	bool3.put("must", mustarr);
    	
    	
    	bool1.put("bool", bool3);
    	query.put("query", bool1);
    	int pageSize = 10;
    	
    	query.put("from",1);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(List<String> list,int pageNo,String rela_p,String isJouOrPate,int flag){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	//JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	JSONArray mustarr = new JSONArray();
    	
//    	JSONObject term1 = new JSONObject();
//    	JSONObject param1 = new JSONObject();
//    	param1.put("relations_p", rela_p);
//    	term1.put("match", param1);
//    	must1.add(term1);
    	
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool4 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("relations_o", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	
    	
    	if(flag==1){
    		JSONObject must_not = new JSONObject();
        	JSONObject terms = new JSONObject();
        	terms.put("source", ".*"+isJouOrPate);
        	must_not.put("regexp", terms);
        	bool2.put("must_not",must_not);
    	}
    	
    	//bool2.put("must",must1);
    	bool4.put("bool", bool2);
    	mustarr.add(bool4);
    	
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	mustarr.add(term1);
    	
    	if(flag==0){
    		JSONObject term2 = new JSONObject();
    		JSONObject param2 = new JSONObject();
    		param2.put("source", ".*"+isJouOrPate);
    		term2.put("regexp", param2);
    		mustarr.add(term2);
    	}
    	
    	bool3.put("must", mustarr);
    	
    	
    	bool1.put("bool", bool3);
    	query.put("query", bool1);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(List<String> list,int pageNo,int pageSize,String rela_p,String isJouOrPate,int flag){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	//JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	
//    	JSONObject term1 = new JSONObject();
//    	JSONObject param1 = new JSONObject();
//    	param1.put("relations_p", rela_p);
//    	term1.put("match", param1);
//    	must1.add(term1);
    	JSONArray mustarr = new JSONArray();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool4 = new JSONObject();
    	JSONArray should = new JSONArray();
    	for(int i = 0;i<list.size();i++){
    		JSONObject term = new JSONObject();
        	JSONObject param = new JSONObject();
        	param.put("relations_o", list.get(i));
        	term.put("match", param);
        	should.add(term);
    	}
    	bool2.put("should", should);
    	
    	
    	if(flag==1 && flag!=2){
    		JSONObject must_not = new JSONObject();
        	JSONObject terms = new JSONObject();
        	terms.put("source", ".*"+isJouOrPate);
        	must_not.put("regexp", terms);
        	bool2.put("must_not",must_not);
    	}
    	
    	//bool2.put("must",must1);
    	bool4.put("bool", bool2);
    	mustarr.add(bool4);
    	
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	mustarr.add(term1);
    	
    	if(flag==0 && flag!=2){
    		JSONObject term2 = new JSONObject();
    		JSONObject param2 = new JSONObject();
    		param2.put("source", ".*"+isJouOrPate);
    		term2.put("regexp", param2);
    		mustarr.add(term2);
    	}
    	
    	bool3.put("must", mustarr);
    	
    	
    	bool1.put("bool", bool3);
    	query.put("query", bool1);
    	
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQueryByRelations_oCGS(String value){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_o", value);
    	term.put("match", param);
    	must1.add(term);
    	
    	bool2.put("must",must1);
    	
    	JSONArray must_not = new JSONArray();
    	JSONObject mn1 = new JSONObject();
    	JSONObject terms = new JSONObject();
    	terms.put("relations_p", "coauthor");
    	mn1.put("term", terms);
    	must_not.add(mn1);
    	
    	JSONObject mn2 = new JSONObject();
    	JSONObject terms2 = new JSONObject();
    	terms2.put("relations_s_index", "t_dm_las_nstl_collection");
    	mn2.put("term", terms2);
    	must_not.add(mn2);
    	
    	bool2.put("must_not",must_not);
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(String value,int pageNo,String rela_p,String rela_s_ii,String rela_s_i,String source,int flag){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_o", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	if(flag==1 && flag!=2){
	    	JSONObject term3 = new JSONObject();
	    	JSONObject param3 = new JSONObject();
	    	param3.put("source", ".*"+source);
	    	term3.put("regexp", param3);
	    	must1.add(term3);
    	}
    	JSONObject term2 = new JSONObject();
    	JSONObject param2 = new JSONObject();
    	param2.put(rela_s_ii, rela_s_i);
    	term2.put("match", param2);
    	must1.add(term2);
    	
    	bool2.put("must",must1);
    	if(flag==0 && flag!=2){
    		JSONObject must_not = new JSONObject();
        	JSONObject terms = new JSONObject();
        	terms.put("source", ".*"+source);
        	must_not.put("regexp", terms);
        	bool2.put("must_not",must_not);
    	}
    	
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	
    	JSONArray sort = new JSONArray();
    	JSONObject uuids = new JSONObject();
    	JSONObject order1s = new JSONObject();
    	order1s.put("order", "desc");
    	uuids.put("publish_year",order1s);
    	sort.add(uuids);
    	query.put("sort",sort);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    
    public static String createQueryBySubjectPaper(String value,String year){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("json.subjClassKwd.subj-class-kwd_kwd", value);
    	term.put("match", param);
    	must1.add(term);
    	if(year!="" && !year.equals("")){
    		JSONObject term1 = new JSONObject();
    		JSONObject param1 = new JSONObject();
    		param1.put("article_year", year);
    		term1.put("match", param1);
    		must1.add(term1);
    	}
    	JSONObject range = new JSONObject();
		JSONObject datet = new JSONObject();
		JSONObject fromto = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		String currentyear = sdf.format(new Date());
		int endtime =  new Integer(currentyear);
		int starttime = new Integer(currentyear) - 9;
		fromto.put("from", starttime+"");
		fromto.put("to", currentyear);
		datet.put("article_year", fromto);  
		range.put("range", datet);
		must1.add(range);
    	
    	bool2.put("must",must1);
    	
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	int pageSize = 1;
    	int pageNo = 0;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "json.subjClassKwd.subj-class-kwd_kwd");
    	t1.put("size", 6);
    	t1.put("execution_hint", "map");
    	t1.put("collect_mode", "breadth_first");
    	p1.put("terms", t1);
    	
    	JSONObject subagg = new JSONObject();
    	JSONObject messages = new JSONObject();
    	JSONObject filters1 = new JSONObject();
    	JSONObject filters = new JSONObject();
    	JSONObject subterm = new JSONObject();
    	
    	
    	for(int i = starttime;i<=endtime;i++){
    		JSONObject term1 = new JSONObject();
        	JSONObject nian1 = new JSONObject();
        	nian1.put("article_year", i+"");
        	term1.put("term", nian1);
        	subterm.put(i+"", term1);
    	}
    	filters1.put("filters", subterm);
    	filters.put("filters", filters1);
    	messages.put("messages", filters);
    	
    	p1.put("aggs", messages);
    	
    	args.put("subj-class-kwd_kwd", p1);
    	query.put("aggs",args);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(String value,String isAll,int pageNo,String rela_p,String rela_s_ii,String rela_s_i,String source,int flag){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_o", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	if(flag==1 && flag!=2){
	    	JSONObject term3 = new JSONObject();
	    	JSONObject param3 = new JSONObject();
	    	param3.put("source", ".*"+source);
	    	term3.put("regexp", param3);
	    	must1.add(term3);
    	}
    	JSONObject term2 = new JSONObject();
    	JSONObject param2 = new JSONObject();
    	param2.put(rela_s_ii, rela_s_i);
    	term2.put("match", param2);
    	must1.add(term2);
    	
    	if(isAll.equals("no")){
    		JSONObject range = new JSONObject();
    		JSONObject datet = new JSONObject();
    		JSONObject fromto = new JSONObject();
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    		String currentyear = sdf.format(new Date());
    		//获取七天前的日期
    		Calendar calendar = Calendar.getInstance();  
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 7);  
            Date today = calendar.getTime();  
            String qiHou = sdf.format(today);  
    		fromto.put("from", qiHou+"");
    		fromto.put("to", currentyear);
    		
    		datet.put("date", fromto);  
    		range.put("range", datet);
    		must1.add(range);
    	}
    	
    	bool2.put("must",must1);
    	if(flag==0 && flag!=2){
    		JSONObject must_not = new JSONObject();
        	JSONObject terms = new JSONObject();
        	terms.put("source", ".*"+source);
        	must_not.put("regexp", terms);
        	bool2.put("must_not",must_not);
    	}
    	
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(String value,int pageNo,int pageSize,String rela_p,String rela_s_ii,String rela_s_i,String source,int flag){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_o", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	if(flag==1  && flag!=2){
	    	JSONObject term3 = new JSONObject();
	    	JSONObject param3 = new JSONObject();
	    	param3.put("source", ".*"+source);
	    	term3.put("regexp", param3);
	    	must1.add(term3);
    	}
    	JSONObject term2 = new JSONObject();
    	JSONObject param2 = new JSONObject();
    	param2.put(rela_s_ii, rela_s_i);
    	term2.put("match", param2);
    	must1.add(term2);
    	
    	bool2.put("must",must1);
    	if(flag==0  && flag!=2){
    		JSONObject must_not = new JSONObject();
        	JSONObject terms = new JSONObject();
        	terms.put("source", ".*"+source);
        	must_not.put("regexp", terms);
        	bool2.put("must_not",must_not);
    	}
    	
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(String value,int pageNo,int pageSize,String rela_p,String rela_s_ii,String rela_s_i,String source){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_o", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
//    	JSONObject term3 = new JSONObject();
//    	JSONObject param3 = new JSONObject();
//    	param3.put("source", ".*"+source+".*"); 
//    	term3.put("regexp", param3);
//    	must1.add(term3);
    	
    	JSONObject term2 = new JSONObject();
    	JSONObject param2 = new JSONObject();
    	param2.put(rela_s_ii, rela_s_i);
    	term2.put("match", param2);
    	must1.add(term2);
    	
    	bool2.put("must",must1);
    	
    	JSONObject must_not = new JSONObject();
    	JSONObject terms = new JSONObject();
    	terms.put("source", ".*"+source);
    	must_not.put("regexp", terms);
    	bool2.put("must_not",must_not);
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	query.put("from",pageNo);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(String value,int pageNo,String rela_p,String rela_s_ii,String rela_s_i){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_o", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	
    	JSONObject term2 = new JSONObject();
    	JSONObject param2 = new JSONObject();
    	param2.put(rela_s_ii, rela_s_i);
    	term2.put("match", param2);
    	must1.add(term2);
    	
    	bool2.put("must",must1);
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(String value,int pageNo,int pageSize,String rela_p){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_o", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_o(String value,int pageNo,int pageSize,String rela_p,String px){
    	JSONObject query = new JSONObject();
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_o", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	
    	JSONArray sort = new JSONArray();
    	JSONObject uuids = new JSONObject();
    	JSONObject order1s = new JSONObject();
    	order1s.put("order", "desc");
    	uuids.put(px,order1s);
    	sort.add(uuids);
    	query.put("sort",sort);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_s(String value){
    	JSONObject query = new JSONObject();
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
//    	value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	param.put("relations_s", value);
    	term.put("match", param);
    	query.put("query", term);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryByRelations_s(String value,int pageNo,String rela_p){
    	JSONObject query = new JSONObject();
    
//    	value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_s", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    	
    }
    public static String createQqueryByRelations_s(String value,int pageNo,int pageSize,String rela_p,String rela_o,String rela_o_i){
    	JSONObject query = new JSONObject();
    
//    	value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_s", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
//    	JSONObject term2 = new JSONObject();
//    	JSONObject param2 = new JSONObject();
//    	param2.put(rela_o,".*"+ rela_o_i+".*");
//    	term2.put("regexp", param2);
//    	must1.add(term2);
    	
    	bool2.put("must",must1);
    	
    	JSONObject must_not = new JSONObject();
    	JSONObject terms = new JSONObject();
    	terms.put(rela_o, ".*"+rela_o_i);
    	must_not.put("regexp", terms);
    	bool2.put("must_not",must_not);
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	//int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    	
    }
    
    public static String createQqueryByRelations_s(String value,String rela_p,String rela_o,String rela_o_i){
    	JSONObject query = new JSONObject();
    
//    	value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_s", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	JSONObject term2 = new JSONObject();
    	JSONObject param2 = new JSONObject();
    	param2.put(rela_o,".*"+ rela_o_i+".*");
    	term2.put("regexp", param2);
    	must1.add(term2);
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	int pageSize = 100;
    	
    	query.put("from",1);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    	
    }
    public static String createQqueryByRelations_s(String value,int pageNo,int pageSize,String rela_p){
    	JSONObject query = new JSONObject();
    
//    	value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_s", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
//    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    	
    }
    public static String createQqueryByRelations_s(String value,int pageNo,int pageSize,String rela_p,String px){
    	JSONObject query = new JSONObject();
    
//    	value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	
    	JSONObject term = new JSONObject();
    	JSONObject param = new JSONObject();
    	param.put("relations_s", value);
    	term.put("match", param);
    	must1.add(term);
    	JSONObject term1 = new JSONObject();
    	JSONObject param1 = new JSONObject();
    	param1.put("relations_p", rela_p);
    	term1.put("match", param1);
    	must1.add(term1);
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
//    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	
    	JSONArray sort = new JSONArray();
    	JSONObject uuids = new JSONObject();
    	JSONObject order1s = new JSONObject();
    	order1s.put("order", "desc");
    	uuids.put(px,order1s);
    	sort.add(uuids);
    	query.put("sort",sort);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    	
    }
    public static String createQqueryPatentJson(String value,String date,String inventor,String applicant,String gj,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	match_all.put("match_all", param);
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();    
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("uuid",order1);         
    	sort.add(uuid);
    	query.put("sort",sort);
    	
    	
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "contrib_full-name-inventor_bg");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("contrib_full-name-inventor_bg", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "article_app-year_bg");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("article_app-year_bg", p2);
//    	JSONObject p3 = new JSONObject();
//    	JSONObject t3 = new JSONObject();
//    	t3.put("field", "contrib_full-name-applicant_bg");
//    	t3.put("size", 10);
//    	p3.put("terms", t3);
//    	args.put("contrib_full-name-applicant_bg", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "contrib_country_country");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("contrib_country_country", p4);
    	query.put("aggs",args);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	must1.add(match_all);
    	
    	JSONObject sources = new JSONObject();
    	JSONObject regexp = new JSONObject();
    	sources.put("source", ".*Patent");
    	regexp.put("regexp", sources);
    	must1.add(regexp);
    	
		if((!date.equals("") && date != null && !date.equals("null"))){
			JSONObject yz = new JSONObject();
			JSONObject entity_type = new JSONObject();
			entity_type.put("article_app-year_bg", date);
			yz.put("match", entity_type);
			must1.add(yz);
		}
		if((!inventor.equals("") && inventor != null && !inventor.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("contrib_full-name-inventor_bg", inventor);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if((!applicant.equals("") && applicant != null && !applicant.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("contrib_full-name-applicant_bg", applicant);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if((!gj.equals("") && gj != null && !gj.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("contrib_country_country", gj);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	//match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
//    	query.put("query", match_all);
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryCountAll(){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	//param.put("uuid", value);
    	match_all.put("match_all", param);
    	
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	must1.add(match_all);
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	//match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
//    	query.put("query", match_all);
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
//生成查询的json
    public static String createQqueryInsJson(String value,String research,String city,String gj,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	//param.put("uuid", value);
    	match_all.put("match_all", param);
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();    
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("contrib_institution_research-domain",order1);
    	sort.add(uuid);
    	query.put("sort",sort);
    	//match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	match_all = JSONObject.parseObject("{\"match_all\": {}}");
    	must1.add(match_all);
    	
    	/*JSONObject sources = new JSONObject();
    	JSONObject regexp = new JSONObject();
    	sources.put("uuid", "zzzzzzzzzztestdata.*");
    	regexp.put("regexp", sources);
    	must1.add(regexp);*/
    	
    	if ((!research.equals("") && research != null && !research.equals("null"))) {
			JSONObject yz = new JSONObject();
			JSONObject entity_type = new JSONObject();
			entity_type.put("contrib_institution_research-domain", research);
			yz.put("match", entity_type);
			must1.add(yz);
		}
		if ((!city.equals("") && city != null && !city.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("contrib_institution_city", city);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if ((!gj.equals("") && gj != null && !gj.equals("null"))) {
			JSONObject yz = new JSONObject();
			JSONObject entity_type = new JSONObject();
			entity_type.put("contrib_institution_country", gj);
			yz.put("match", entity_type);
			must1.add(yz);
		}
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
		
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "contrib_institution_research-domain");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("contrib_institution_research-domain", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "contrib_institution_city");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("contrib_institution_city", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "contrib_institution_country");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("contrib_institution_country", p3);
    	query.put("aggs",args);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryPaJson(String source,String author,String date,String jou,String ins,String sub,String lwlx,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	//param.put("uuid", value);
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "contrib_full-name");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("contrib_full-name", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "article_year");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("article_year", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "contrib_institution");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("contrib_institution", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "source_source-title");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("source_source-title", p4);
    	JSONObject p5 = new JSONObject();
    	JSONObject t5 = new JSONObject();
    	t5.put("field", "json.subjClassKwd.subj-class-kwd_kwd");
    	t5.put("size", 10);
    	p5.put("terms", t5);
    	args.put("subj-class-kwd_kwd", p5);
    	JSONObject p6 = new JSONObject();
    	JSONObject t6 = new JSONObject();
    	t6.put("field", "source_source-type");
    	t6.put("size", 10);
    	p6.put("terms", t6);
    	args.put("source_source-type", p6);
    	query.put("aggs",args);
    	
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	match_all.put("match_all", param);
    	must1.add(match_all);
    	
    	JSONObject sources = new JSONObject();
    	JSONObject regexp = new JSONObject();
    	sources.put("source", ".*Article.*");
    	regexp.put("regexp", sources);
    	must1.add(regexp);
    	
		if (!author.equals("") && author != null && !author.equals("null")) {
			JSONObject match = new JSONObject();
			JSONObject uuid = new JSONObject();
			uuid.put("contrib_full-name", author);
			match.put("match", uuid);
			must1.add(match);
		}
		if (!date.equals("") && date != null && !date.equals("null")) {
			JSONObject match = new JSONObject();
			JSONObject dates = new JSONObject();
			dates.put("article_year", date);
			match.put("match", dates);
			must1.add(match);
		}
		if (!jou.equals("") && jou != null && !jou.equals("null")) {
			JSONObject match = new JSONObject();
			JSONObject jous = new JSONObject();
			jous.put("source_source-title", jou);
			match.put("match", jous);
			must1.add(match);
		}
		if (!ins.equals("") && ins != null && !ins.equals("null")) {
			JSONObject match = new JSONObject();
			JSONObject inss = new JSONObject();
			inss.put("contrib_institution", ins);
			match.put("match", inss);
			must1.add(match);
		}
		if (!sub.equals("") && sub != null && !sub.equals("null")) {
			JSONObject match = new JSONObject();
			JSONObject subs = new JSONObject();
			subs.put("json.subjClassKwd.subj-class-kwd_kwd", sub);
			match.put("match", subs);
			must1.add(match);
		}
		if (!lwlx.equals("") && lwlx != null && !lwlx.equals("null")) {
			JSONObject match = new JSONObject();
			JSONObject subs = new JSONObject();
			subs.put("source_source-type", lwlx);
			match.put("match", subs);
			must1.add(match);
		}
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();    
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("uuid",order1);         
    	sort.add(uuid);
    	query.put("sort",sort);
    	
    	//match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}

    	
    	
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryJouJson(String value,String yuZhong,String dataBase,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	//param.put("uuid", value);
    	match_all.put("match_all", param);
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "asc");
    	uuid.put("source_pub_frequency",order1);
    	sort.add(uuid);
    	query.put("sort",sort);
    	//match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	must1.add(match_all);
			if((!yuZhong.equals("") && yuZhong != null && !yuZhong.equals("null"))){
				JSONObject yz = new JSONObject();
				JSONObject entity_type = new JSONObject();
				entity_type.put("source_lang", yuZhong);
				yz.put("match", entity_type);
				must1.add(yz);
			}
			if((!dataBase.equals("") && dataBase != null && !dataBase.equals("null"))){
				JSONObject sjk = new JSONObject();
				JSONObject source_title_sec_type = new JSONObject();
				source_title_sec_type.put("access_database-name", dataBase);
				sjk.put("match", source_title_sec_type);
				must1.add(sjk);
			}
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "source_lang");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("source_lang", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "access_database-name");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("access_database-name", p2);
    	query.put("aggs",args);
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryZiXunJsonGL(String value,String siteClassify,String siteName,String siteCountry ,String contentClassify,String year, int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	
//    	JSONObject highlight = new JSONObject();
//    	JSONObject fields = new JSONObject();
//    	JSONObject ss = new JSONObject();
//    	fields.put("contrib_full-name", ss);
//    	highlight.put("fields", fields);
//    	query.put("highlight", highlight);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "siteClassify");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("siteClassify", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "siteName");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("siteName", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "siteCountry");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("siteCountry", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "majorContentClassify");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("contentClassify", p4);
    	JSONObject p5 = new JSONObject();
    	JSONObject t5 = new JSONObject();
    	t5.put("field", "year");
    	t5.put("size", 10);
    	p5.put("terms", t5);
    	args.put("year", p5);
    	query.put("aggs",args);
    	
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
//    	JSONObject contrib_major1 = new JSONObject();
//    	JSONObject order1 = new JSONObject();
//    	order1.put("order", "desc");
//    	contrib_major1.put("contrib_role",order1);
//    	sort.add(contrib_major1);
//    	JSONObject uuid = new JSONObject();
//    	JSONObject order2 = new JSONObject();
//    	order2.put("order", "desc");
//    	uuid.put("uuid",order2);
//    	sort.add(uuid);
    	query.put("sort",sort);
    	
    	
//    	match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
		if (value != null && !value.equals("") && !value.equals("null")) {

	    	JSONObject multi_match = new JSONObject();
	    	JSONObject param = new JSONObject();
	    	String[] arr = null;
	    	arr = new String[]{
	    	              		"title_ik^9",
	    	              		"enAbstract"
	    	              		};
	    	param.put("query", value);
	    	//param.put("operator", "and");
	    	param.put("fields", arr);
	    	param.put("type", "phrase");
	    	multi_match.put("multi_match", param);
	    	must1.add(multi_match);
//			JSONObject match_all = new JSONObject();
//	    	JSONObject param = new JSONObject();
//	    	param.put("title", value);
//	    	match_all.put("match", param);
//	    	must1.add(match_all);
		}else{
			JSONObject match_all = new JSONObject();
			match_all = JSONObject.parseObject("{\"match_all\": {}}");
	    	must1.add(match_all);
		}
    	JSONObject exists1 = new JSONObject();
    	JSONObject field1 = new JSONObject();
    	field1.put("field", "title");
    	exists1.put("exists", field1);
    	must1.add(exists1);
		
		JSONObject exists = new JSONObject();
        JSONObject field = new JSONObject();
        field.put("field", "uuid");
        exists.put("exists",field);
        must1.add(exists);
		if ((!year.equals("") && year != null && !year.equals("null"))) {
			
			JSONObject yz = new JSONObject();
			JSONObject contrib_major = new JSONObject();
			contrib_major.put("year", year);
			yz.put("match", contrib_major);
			must1.add(yz);
		
		}
		if ((!siteClassify.equals("") && siteClassify != null && !siteClassify.equals("null"))) {
			
				JSONObject yz = new JSONObject();
				JSONObject contrib_major = new JSONObject();
				contrib_major.put("siteClassify", siteClassify);
				yz.put("match", contrib_major);
				must1.add(yz);
			
		}
		if ((!siteName.equals("") && siteName != null && !siteName.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("siteName", siteName);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
			
		}
		if ((!siteCountry.equals("") && siteCountry != null && !siteCountry.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("siteCountry", siteCountry);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
			
		}
		
		if ((!contentClassify.equals("") && contentClassify != null && !contentClassify.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("majorContentClassify", contentClassify);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
			
		}
//		JSONObject is_display = new JSONObject();
//		JSONObject is_displaymatch = new JSONObject();
//		is_displaymatch.put("is_display", 1);
//		is_display.put("match", is_displaymatch);
//		must1.add(is_display);
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryExpertJsonGL(String value,String subject,String ins,String role,String researchSubject, int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	
    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	fields.put("contrib_full-name", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "contrib_major");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("contrib_major", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "contrib_institution_bg");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("contrib_institution", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "contrib_role");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("contrib_role", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "contrib_research-subject");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("contrib_research-subject", p4);
    	query.put("aggs",args);
    	
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject contrib_major1 = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	contrib_major1.put("contrib_role",order1);
    	sort.add(contrib_major1);
//    	JSONObject uuid = new JSONObject();
//    	JSONObject order2 = new JSONObject();
//    	order2.put("order", "desc");
//    	uuid.put("uuid",order2);
//    	sort.add(uuid);
    	query.put("sort",sort);
    	
    	
//    	match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
		if (value != null && !value.equals("") && !value.equals("null")) {

	    	JSONObject multi_match = new JSONObject();
	    	JSONObject param = new JSONObject();
	    	String[] arr = null;
	    	arr = new String[]{
	    	              		"contrib_full-name_ik",
	    	              		"contrib_full-name^50",
	    						"contrib_institution_ik",
	    						"contrib_research-subject_ik"};
	    	param.put("query", value);
	    	param.put("operator", "and");
	    	param.put("fields", arr);
	    	param.put("type", "cross_fields");
	    	multi_match.put("multi_match", param);
	    	must1.add(multi_match);
//			JSONObject match_all = new JSONObject();
//	    	JSONObject param = new JSONObject();
//	    	param.put("contrib_full-name", ".*"+value+".*");
//	    	match_all.put("regexp", param);
//	    	must1.add(match_all);
		}else{
			JSONObject match_all = new JSONObject();
			match_all = JSONObject.parseObject("{\"match_all\": {}}");
	    	must1.add(match_all);
		}
//    	JSONObject sources = new JSONObject();
//    	JSONObject regexp = new JSONObject();
//    	sources.put("contrib_full-name", ".*"+value+".*");
//    	regexp.put("regexp", sources);
//    	must1.add(regexp);
		if ((!subject.equals("") && subject != null && !subject.equals("null"))) {
			if(subject.contains(",")){
				String[] split = subject.split(",");
				for(String s : split){
					JSONObject yz = new JSONObject();
					JSONObject contrib_major = new JSONObject();
					contrib_major.put("contrib_major", s);
					yz.put("match", contrib_major);
					must1.add(yz);
				}
			}else{
				JSONObject yz = new JSONObject();
				JSONObject contrib_major = new JSONObject();
				contrib_major.put("contrib_major", subject);
				yz.put("match", contrib_major);
				must1.add(yz);
			}
			
		}
		if ((!ins.equals("") && ins != null && !ins.equals("null"))) {
			if(ins.contains(",")){
				String[] split = ins.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject contrib_collab = new JSONObject();
					contrib_collab.put("contrib_institution_bg", s);
					sjk.put("match", contrib_collab);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject contrib_collab = new JSONObject();
				contrib_collab.put("contrib_institution_bg", ins);
				sjk.put("match", contrib_collab);
				must1.add(sjk);
			}
			
		}
		if ((!role.equals("") && role != null && !role.equals("null"))) {
			if(role.contains(",")){
				String[] split = role.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject contrib_collab = new JSONObject();
					contrib_collab.put("contrib_role", s);
					sjk.put("match", contrib_collab);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject contrib_collab = new JSONObject();
				contrib_collab.put("contrib_role", role);
				sjk.put("match", contrib_collab);
				must1.add(sjk);
			}
			
		}
		
		if ((!researchSubject.equals("") && researchSubject != null && !researchSubject.equals("null"))) {
			
			if(researchSubject.contains(",")){
				String[] split = researchSubject.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject contrib_collab = new JSONObject();
					contrib_collab.put("contrib_research-subject", s);
					sjk.put("match", contrib_collab);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject contrib_collab = new JSONObject();
				contrib_collab.put("contrib_research-subject", researchSubject);
				sjk.put("match", contrib_collab);
				must1.add(sjk);
			}
			
		}
		JSONObject is_display = new JSONObject();
		JSONObject is_displaymatch = new JSONObject();
		is_displaymatch.put("is_display", 1);
		is_display.put("match", is_displaymatch);
		must1.add(is_display);
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryExpertJsonGL(String value,String subject,String ins,String role,String researchSubject, int pageNo,int pageSize,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	
    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	fields.put("contrib_full-name", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "contrib_major");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("contrib_major", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "contrib_institution_bg");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("contrib_institution", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "contrib_role");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("contrib_role", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "contrib_research-subject");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("contrib_research-subject", p4);
    	query.put("aggs",args);
    	
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject contrib_major1 = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	contrib_major1.put("contrib_role",order1);
    	sort.add(contrib_major1);
    	JSONObject uuid = new JSONObject();
    	JSONObject order2 = new JSONObject();
    	order2.put("order", "desc");
    	uuid.put("uuid",order2);
    	sort.add(uuid);
    	query.put("sort",sort);
    	
    	
//    	match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
		if (value != null && !value.equals("") && !value.equals("null")) {

	    	JSONObject multi_match = new JSONObject();
	    	JSONObject param = new JSONObject();
	    	String[] arr = null;
	    	arr = new String[]{
	    	              		"contrib_full-name_ik",
	    	              		"contrib_full-name^15",
	    	              		"contrib_institution^5",
	    						"contrib_institution_ik",
	    						"contrib_research-subject_ik"};
	    	param.put("query", value);
	    	param.put("operator", "and");
	    	param.put("fields", arr);
	    	param.put("type", "cross_fields");
	    	multi_match.put("multi_match", param);
	    	must1.add(multi_match);
//			JSONObject match_all = new JSONObject();
//	    	JSONObject param = new JSONObject();
//	    	param.put("contrib_full-name", ".*"+value+".*");
//	    	match_all.put("regexp", param);
//	    	must1.add(match_all);
		}else{
			JSONObject match_all = new JSONObject();
			match_all = JSONObject.parseObject("{\"match_all\": {}}");
	    	must1.add(match_all);
		}
//    	JSONObject sources = new JSONObject();
//    	JSONObject regexp = new JSONObject();
//    	sources.put("contrib_full-name", ".*"+value+".*");
//    	regexp.put("regexp", sources);
//    	must1.add(regexp);
		if ((!subject.equals("") && subject != null && !subject.equals("null"))) {
			if(subject.contains(",")){
				String[] split = subject.split(",");
				for(String s : split){
					JSONObject yz = new JSONObject();
					JSONObject contrib_major = new JSONObject();
					contrib_major.put("contrib_major", s);
					yz.put("match", contrib_major);
					must1.add(yz);
				}
			}else{
				JSONObject yz = new JSONObject();
				JSONObject contrib_major = new JSONObject();
				contrib_major.put("contrib_major", subject);
				yz.put("match", contrib_major);
				must1.add(yz);
			}
			
		}
		if ((!ins.equals("") && ins != null && !ins.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("contrib_institution_bg", ins);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
		}
		if ((!role.equals("") && role != null && !role.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("contrib_role", role);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
		}
		
		if ((!researchSubject.equals("") && researchSubject != null && !researchSubject.equals("null"))) {
			if(researchSubject.contains(",")){
				String[] split = researchSubject.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject contrib_collab = new JSONObject();
					contrib_collab.put("contrib_research-subject", s);
					sjk.put("match", contrib_collab);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject contrib_collab = new JSONObject();
				contrib_collab.put("contrib_research-subject", researchSubject);
				sjk.put("match", contrib_collab);
				must1.add(sjk);
			}
			
		}
		JSONObject is_display = new JSONObject();
		JSONObject is_displaymatch = new JSONObject();
		is_displaymatch.put("is_display", 1);
		is_display.put("match", is_displaymatch);
		must1.add(is_display);
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryExpertJsonGLAgg(String value,String subject,String ins,String role,String researchSubject, int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	
    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	fields.put("contrib_full-name", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	JSONObject args = new JSONObject();
//    	JSONObject p1 = new JSONObject();
//    	JSONObject t1 = new JSONObject();
//    	t1.put("field", "contrib_major");
//    	t1.put("size", 10);
//    	p1.put("terms", t1);
//    	args.put("contrib_major", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "contrib_institution");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("contrib_institution", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "contrib_role");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("contrib_role", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "contrib_research-subject");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("contrib_research-subject", p4);
    	query.put("aggs",args);
    	
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("contrib_major",order1);
    	sort.add(uuid);
    	query.put("sort",sort);
    	
    	
//    	match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
		if (value != null && !value.equals("") && !value.equals("null")) {

			JSONObject multi_match = new JSONObject();
	    	JSONObject param = new JSONObject();
	    	String[] arr = null;
	    	arr = new String[]{
	    	              		"contrib_full-name_ik^8",
	    						"contrib_institution_ik"};
	    	param.put("query", value);
	    	param.put("operator", "and");
	    	param.put("fields", arr);
	    	param.put("type", "cross_fields");
	    	multi_match.put("multi_match", param);
	    	must1.add(multi_match);
//		JSONObject match_all = new JSONObject();
//    	JSONObject param = new JSONObject();
//    	param.put("contrib_full-name", ".*"+value+".*");
//    	match_all.put("regexp", param);
//    	must1.add(match_all);
		}else{
			JSONObject match_all = new JSONObject();
			match_all = JSONObject.parseObject("{\"match_all\": {}}");
	    	must1.add(match_all);
		}
//    	JSONObject sources = new JSONObject();
//    	JSONObject regexp = new JSONObject();
//    	sources.put("uuid", "zzzzzzzzzztestdata.*");
//    	regexp.put("regexp", sources);
//    	must1.add(regexp);
    	
		if ((!subject.equals("") && subject != null && !subject.equals("null"))) {
			JSONObject yz = new JSONObject();
			JSONObject contrib_major = new JSONObject();
			contrib_major.put("contrib_major", subject);
			yz.put("match", contrib_major);
			must1.add(yz);
		}
		if ((!ins.equals("") && ins != null && !ins.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("contrib_institution", ins);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
		}
		if ((!role.equals("") && role != null && !role.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("contrib_role", role);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
		}
		
		if ((!researchSubject.equals("") && researchSubject != null && !researchSubject.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("contrib_research-subject", researchSubject);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
		}
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryExpertRgzn(String uuid,int pageNo){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	param.put("uuid", "zzzzzzzzzzztestdata.*");
    	match_all.put("regexp", param);
    	query.put("query", match_all);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    }
    public static String createQqueryJiJinJsonGL(String value,String year,String type,String ins,String author,String xmzz,String lang,String capitalsection,String fundgj,String state,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	
    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	fields.put("award_award-name_ik", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
//    	JSONObject _score1 = new JSONObject();
//    	JSONObject order2 = new JSONObject();
//    	order2.put("order", "desc");
//    	_score1.put("award_award_capitalsection",order2);
//    	sort.add(_score1);
//    	JSONObject uuid = new JSONObject();
//    	JSONObject order1 = new JSONObject();
//    	order1.put("order", "desc");
//    	uuid.put("uuid",order1);
//    	sort.add(uuid);
    	query.put("sort",sort);
    	
//    	match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
		if (value != null && !value.equals("") && !value.equals("null")) {
	    	JSONObject multi_match = new JSONObject();
	    	String[] arr = null;
	    	arr = new String[]{
	    			 "award_award-name_ik^8",
	                 "award_PI_ik^7",
	                 "award_instituition_ik^5"
	    	              	};
	    	param.put("query", value);
	    	param.put("operator", "and");
	    	param.put("type", "cross_fields");
	    	param.put("fields", arr);
	    	//param.put("type", "best_fields");
	    	multi_match.put("multi_match", param);
	    	must1.add(multi_match);
//			param.put("award_award-name", ".*"+value+".*");
//	    	match_all.put("regexp", param);
//	    	must1.add(match_all);
		}else{
			match_all.put("match_all", param);
			must1.add(match_all);
		}
		if (!year.equals("") && year != null && !year.equals("null")) {
			if(year.contains(",")){
				String[] split = year.split(",");
				String starttime = "";
				String endtime = "";
				starttime = split[0];
				endtime = split[1];
				JSONObject nulls = new JSONObject();
				JSONObject range = new JSONObject();
				JSONObject datet = new JSONObject();
				JSONObject fromto = new JSONObject();
				if(!starttime.equals("*") && !endtime.equals("*")){
					fromto.put("from", starttime);
					fromto.put("to", endtime);
				}
				if(!starttime.equals("*") && endtime.equals("*")){
					fromto.put("from", starttime);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
					String currentyear = sdf.format(new Date());
					fromto.put("to", currentyear);
				}
				if(starttime.equals("*") && !endtime.equals("*")){
					fromto.put("from", null);
					fromto.put("to", endtime);
				}
	    		datet.put("award_award-start-year", fromto);  
	    		range.put("range", datet);
	    		must1.add(range);
			}else{
				JSONObject yz = new JSONObject();
				JSONObject award_year = new JSONObject();
				award_year.put("award_award-start-year", year);
				yz.put("match", award_year);
				must1.add(yz);
			}
		}
		/*if((!year.equals("") && year != null && !year.equals("null"))){
			JSONObject yz = new JSONObject();
			JSONObject award_year = new JSONObject();
			award_year.put("award_year", year);
			yz.put("match", award_year);
			must1.add(yz);
		}*/
		if((!type.equals("") && type != null && !type.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject award_award_type = new JSONObject();
			award_award_type.put("award_award-type", type);
			sjk.put("match", award_award_type);
			must1.add(sjk);
		}
		
		if((!ins.equals("") && ins != null && !ins.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject award_award_type = new JSONObject();
			award_award_type.put("award_instituition_bg", ins);
			sjk.put("match", award_award_type);
			must1.add(sjk);
		}
		if((!author.equals("") && author != null && !author.equals("null"))){
			if(author.contains(",")){
				String[] split = author.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject author1 = new JSONObject();
					author1.put("award_subjectClass", s);
					sjk.put("match", author1);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject author1 = new JSONObject();
				author1.put("award_subjectClass", author);
				sjk.put("match", author1);
				must1.add(sjk);
			}
			
		}
		if((!xmzz.equals("") && xmzz != null && !xmzz.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject author1 = new JSONObject();
			author1.put("award_funding_institution_bg", xmzz);
			sjk.put("match", author1);
			must1.add(sjk);
		}
		
		if((!fundgj.equals("") && fundgj != null && !fundgj.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject author1 = new JSONObject();
			author1.put("award_funding_institution_country_bg", fundgj);
			sjk.put("match", author1);
			must1.add(sjk);
		}
		if((!state.equals("") && state != null && !state.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject author1 = new JSONObject();
			author1.put("award_funding-statement", state);
			sjk.put("match", author1);
			must1.add(sjk);
		}
		if((!lang.equals("") && lang != null && !lang.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject author1 = new JSONObject();
			author1.put("award_lang_bg", lang);
			sjk.put("match", author1);
			must1.add(sjk);
		}
		if((!capitalsection.equals("") && capitalsection != null && !capitalsection.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject author1 = new JSONObject();
			author1.put("award_award_capitalsection", capitalsection);
			sjk.put("match", author1);
			must1.add(sjk);
		}
		JSONObject is_display = new JSONObject();
		JSONObject is_displaymatch = new JSONObject();
		is_displaymatch.put("is_display", 1);
		is_display.put("match", is_displaymatch);
		must1.add(is_display);
		
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p11 = new JSONObject();
    	JSONObject t11 = new JSONObject();
    	t11.put("field", "award_award-start-year");
    	t11.put("size", 10);
    	JSONObject temsorder = new JSONObject();
    	temsorder.put("_term", "desc");
    	t11.put("order", temsorder);
    	p11.put("terms", t11);
    	args.put("award_year", p11);
    	JSONObject t1 = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	t1.put("field", "award_award-type");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("award_award-type", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "award_funding_institution_country_bg");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("award_funding_institution_country", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "award_instituition_bg");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("award_instituition", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "award_subjectClass");
    	t4.put("size", 30);
    	p4.put("terms", t4);
    	args.put("award_subjectClass", p4);
    	JSONObject p5 = new JSONObject();
    	JSONObject t5 = new JSONObject();
    	t5.put("field", "award_funding_institution_bg");
    	t5.put("size", 10);
    	p5.put("terms", t5);
    	args.put("award_funding_institution", p5);
    	JSONObject p6 = new JSONObject();
    	JSONObject t6 = new JSONObject();
    	t6.put("field", "award_lang_bg");
    	t6.put("size", 10);
    	p6.put("terms", t6);
    	args.put("award_lang", p6);
    	
    	JSONObject p8 = new JSONObject();
    	JSONObject t8 = new JSONObject();
    	t8.put("field", "award_funding-statement");
    	t8.put("size", 10);
    	p8.put("terms", t8);
    	args.put("award_funding-statement", p8);
    	
    	JSONObject p7 = new JSONObject();
    	JSONObject t7 = new JSONObject();
    	t7.put("field", "award_award_capitalsection");
    	t7.put("size", 10);
    	p7.put("terms", t7);
    	args.put("award_award_capitalsection", p7);
    	query.put("aggs",args);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 2;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQueryJsonConf(String value,String year,String subject,String cdins,String gj,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	query.put("sort",sort);
    	
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
		if (value != null && !value.equals("") && !value.equals("null")) {
	    	JSONObject multi_match = new JSONObject();
	    	String[] arr = null;
	    	arr = new String[]{
	    			 "conf_conf-name_ik",
	                 "conf_conf_subject_ik",
	                 "conf_conf_linkman",
	                 "conf_string-conf_ik"
	    	              	};
	    	param.put("query", value);
	    	param.put("operator", "and");
	    	param.put("type", "cross_fields");
	    	param.put("fields", arr);
	    	//param.put("type", "best_fields");
	    	multi_match.put("multi_match", param);
	    	must1.add(multi_match);
//			param.put("award_award-name", ".*"+value+".*");
//	    	match_all.put("regexp", param);
//	    	must1.add(match_all);
		}else{
			match_all.put("match_all", param);
			must1.add(match_all);
		}
		if (!year.equals("") && year != null && !year.equals("null")) {
			if(year.contains(",")){
				String[] split = year.split(",");
				String starttime = "";
				String endtime = "";
				starttime = split[0];
				endtime = split[1];
				JSONObject nulls = new JSONObject();
				JSONObject range = new JSONObject();
				JSONObject datet = new JSONObject();
				JSONObject fromto = new JSONObject();
				if(!starttime.equals("*") && !endtime.equals("*")){
					fromto.put("from", starttime);
					fromto.put("to", endtime);
				}
				if(!starttime.equals("*") && endtime.equals("*")){
					fromto.put("from", starttime);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
					String currentyear = sdf.format(new Date());
					fromto.put("to", currentyear);
				}
				if(starttime.equals("*") && !endtime.equals("*")){
					fromto.put("from", null);
					fromto.put("to", endtime);
				}
	    		datet.put("conf_year", fromto);  
	    		range.put("range", datet);
	    		must1.add(range);
			}else{
				JSONObject yz = new JSONObject();
				JSONObject award_year = new JSONObject();
				award_year.put("conf_year", year);
				yz.put("match", award_year);
				must1.add(yz);
			}
		}
		/*if((!year.equals("") && year != null && !year.equals("null"))){
			JSONObject yz = new JSONObject();
			JSONObject award_year = new JSONObject();
			award_year.put("award_year", year);
			yz.put("match", award_year);
			must1.add(yz);
		}*/
		
		if((!cdins.equals("") && cdins != null && !cdins.equals("null"))){
			if(cdins.contains(",")){
				String[] split = cdins.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject award_award_type = new JSONObject();
					award_award_type.put("conf_undertake_instituition", s);
					sjk.put("match", award_award_type);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject award_award_type = new JSONObject();
				award_award_type.put("conf_undertake_instituition", cdins);
				sjk.put("match", award_award_type);
				must1.add(sjk);
			}
			
		}
		if((!subject.equals("") && subject != null && !subject.equals("null"))){
			if(subject.contains(",")){
				String[] split = subject.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject author1 = new JSONObject();
					author1.put("conf_conf_subject", s);
					sjk.put("match", author1);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject author1 = new JSONObject();
				author1.put("conf_conf_subject", subject);
				sjk.put("match", author1);
				must1.add(sjk);
			}
			
		}
		if((!gj.equals("") && gj != null && !gj.equals("null"))){
			if(gj.contains(",")){
				String[] split = gj.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject author1 = new JSONObject();
					author1.put("conf_country", s);
					sjk.put("match", author1);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject author1 = new JSONObject();
				author1.put("conf_country", gj);
				sjk.put("match", author1);
				must1.add(sjk);
			}
			
		}
		
		JSONObject is_display = new JSONObject();
		JSONObject is_displaymatch = new JSONObject();
		is_displaymatch.put("is_display", 1);
		is_display.put("match", is_displaymatch);
		must1.add(is_display);
		
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p11 = new JSONObject();
    	JSONObject t11 = new JSONObject();
    	t11.put("field", "conf_year");
    	t11.put("size", 10);
    	p11.put("terms", t11);
    	args.put("conf_year", p11);
    	JSONObject t1 = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	t1.put("field", "conf_country");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("conf_country", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "conf_undertake_instituition");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("conf_undertake_instituition", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "conf_conf_subject");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("conf_conf_subject", p3);
    	query.put("aggs",args);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 2;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
//    	query.put("from",3);
//    	query.put("size", 1);
    	System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryJiJinJsonGLAgg(String value,String year,String type,String ins,String author,String xmzz,String lang,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	
    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	fields.put("award_award-name_ik", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	query.put("sort",sort);
    	
//    	match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
		if (value != null && !value.equals("") && !value.equals("null")) {

//	    	JSONObject multi_match = new JSONObject();
//	    	String[] arr = null;
//	    	arr = new String[]{
//	    	              	"award_award-name_ik"};
//	    	param.put("query", value);
//	    	param.put("operator", "and");
//	    	param.put("fields", arr);
//	    	//param.put("type", "best_fields");
//	    	multi_match.put("multi_match", param);
//	    	must1.add(multi_match);
			param.put("award_award-name", ".*"+value+".*");
	    	match_all.put("regexp", param);
	    	must1.add(match_all);
		}else{
			match_all.put("match_all", param);
			must1.add(match_all);
		}
		if (!year.equals("") && year != null && !year.equals("null")) {
			if(year.contains(",")){
				String[] split = year.split(",");
				String starttime = "";
				String endtime = "";
				starttime = split[0];
				endtime = split[1];
				JSONObject nulls = new JSONObject();
				JSONObject range = new JSONObject();
				JSONObject datet = new JSONObject();
				JSONObject fromto = new JSONObject();
				if(!starttime.equals(" ") && !endtime.equals(" ")){
					fromto.put("from", starttime);
					fromto.put("to", endtime);
				}
				if(!starttime.equals(" ") && endtime.equals(" ")){
					fromto.put("from", starttime);
					fromto.put("to", null);
				}
				if(starttime.equals(" ") && !endtime.equals(" ")){
					fromto.put("from", null);
					fromto.put("to", endtime);
				}
	    		datet.put("award_year", fromto);  
	    		range.put("range", datet);
	    		must1.add(range);
			}else{
				JSONObject yz = new JSONObject();
				JSONObject award_year = new JSONObject();
				award_year.put("award_year", year);
				yz.put("match", award_year);
				must1.add(yz);
			}
		}
		/*if((!year.equals("") && year != null && !year.equals("null"))){
			JSONObject yz = new JSONObject();
			JSONObject award_year = new JSONObject();
			award_year.put("award_year", year);
			yz.put("match", award_year);
			must1.add(yz);
		}*/
		if((!type.equals("") && type != null && !type.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject award_award_type = new JSONObject();
			award_award_type.put("award_award-type", type);
			sjk.put("match", award_award_type);
			must1.add(sjk);
		}
		if((!ins.equals("") && ins != null && !ins.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject award_award_type = new JSONObject();
			award_award_type.put("award_instituition", ins);
			sjk.put("match", award_award_type);
			must1.add(sjk);
		}
		if((!author.equals("") && author != null && !author.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject author1 = new JSONObject();
			author1.put("award_subjectClass", author);
			sjk.put("match", author1);
			must1.add(sjk);
		}
		if((!xmzz.equals("") && xmzz != null && !xmzz.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject author1 = new JSONObject();
			author1.put("award_funding_institution", xmzz);
			sjk.put("match", author1);
			must1.add(sjk);
		}
		if((!lang.equals("") && lang != null && !lang.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject author1 = new JSONObject();
			author1.put("award_lang", lang);
			sjk.put("match", author1);
			must1.add(sjk);
		}
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "award_award-type");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("award_award-type", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "award_funding_institution_country");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("award_funding_institution_country", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "award_instituition");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("award_instituition", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "award_subjectClass");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("award_subjectClass", p4);
    	JSONObject p5 = new JSONObject();
    	JSONObject t5 = new JSONObject();
    	t5.put("field", "award_funding_institution");
    	t5.put("size", 10);
    	p5.put("terms", t5);
    	args.put("award_funding_institution", p5);
    	JSONObject p6 = new JSONObject();
    	JSONObject t6 = new JSONObject();
    	t6.put("field", "award_lang");
    	t6.put("size", 10);
    	p6.put("terms", t6);
    	args.put("award_lang", p6);
    	query.put("aggs",args);
    	
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 2;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    
    public static String createQqueryJouJsonGL(String value,String yuZhong,String dataBase,String subClass,String initial,String access_license,String frequency,String collectionType,int pageNo,String orderBy,String method){
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
		
    	
    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	fields.put("source_source-title", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("source_lasc",order1);
    	sort.add(uuid);
//    	JSONObject uuid1 = new JSONObject();
//    	JSONObject order2 = new JSONObject();
//    	order2.put("order", "desc");
//    	uuid1.put("uuid",order2);
//    	sort.add(uuid1);
    	query.put("sort",sort);
    	//source_pub_frequency
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "source_lang_bg");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("source_lang", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "access_database-name");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("access_database-name", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "source_lasc");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("source_lasc", p3);
    	JSONObject p5 = new JSONObject();
    	JSONObject t5 = new JSONObject();
    	t5.put("field", "source_pub_frequency");
    	t5.put("size", 10);
    	p5.put("terms", t5);
    	args.put("source_pub_frequency", p5);
    	
    	JSONObject p6 = new JSONObject();
    	JSONObject t6 = new JSONObject();
    	t6.put("field", "collection_type");
    	t6.put("size", 10);
    	p6.put("terms", t6);
    	args.put("collection_type", p6);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "source_source_initial");
    	t4.put("size", 30);
    	p4.put("terms", t4);
    	args.put("source_source_initial", p4);
    	
    	query.put("aggs",args);
    	
    	
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	if (value != null && !value.equals("") && !value.equals("null")) {
//			param.put("source_source-title",".*"+ value+".*");
//	    	match_all.put("regexp", param);
//    		must1.add(match_all);
    		
    		JSONObject multi_match = new JSONObject();
        	String[] arr = null;
        	arr = new String[]{
        	              	"source_source-title_ik^4",
        	              	"source_source-title^18",
        	              	"access_database-name_ik^5",
                            "source_source-title-en_ik",
                            "source_source-title-other_ik^10",
        	              	"source_issn",
        					"source_managementInstituition_ik",
        					"source_hostInstituition_ik"
        	              	};
        	param.put("query", value);
        	param.put("operator", "and");
        	param.put("type", "cross_fields");
        	param.put("fields", arr);
        	//param.put("type", "best_fields");
        	multi_match.put("multi_match", param);
        	must1.add(multi_match);
		}else{
			match_all.put("match_all", param);
			must1.add(match_all);
			
			
		}
    	
    	/*JSONObject sources = new JSONObject();
    	JSONObject regexp = new JSONObject();
    	sources.put("source_source-type", "journal");
    	regexp.put("match", sources);
    	must1.add(regexp);*/
    	
		if ((!yuZhong.equals("") && yuZhong != null && !yuZhong.equals("null"))) {
			
			if(yuZhong.contains(",")){
				String[] split = yuZhong.split(",");
				for(String s : split){
					JSONObject yz = new JSONObject();
					JSONObject entity_type = new JSONObject();
					entity_type.put("source_lang_bg", s);
					yz.put("match", entity_type);
					must1.add(yz);
				}
			}else{
				JSONObject yz = new JSONObject();
				JSONObject entity_type = new JSONObject();
				entity_type.put("source_lang_bg", yuZhong);
				yz.put("match", entity_type);
				must1.add(yz);
			}
			
		}
		if ((!dataBase.equals("") && dataBase != null && !dataBase.equals("null"))) {
			if(dataBase.contains(",")){
				String[] split = dataBase.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject source_title_sec_type = new JSONObject();
					source_title_sec_type.put("access_database-name", s);
					sjk.put("match", source_title_sec_type);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject source_title_sec_type = new JSONObject();
				source_title_sec_type.put("access_database-name", dataBase);
				sjk.put("match", source_title_sec_type);
				must1.add(sjk);
			}
			
		}
		if ((!subClass.equals("") && subClass != null && !subClass.equals("null"))) {
			if(subClass.contains(",")){
				String[] split = subClass.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject source_title_sec_type = new JSONObject();
					source_title_sec_type.put("source_lasc", s);
					sjk.put("match", source_title_sec_type);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject source_title_sec_type = new JSONObject();
				source_title_sec_type.put("source_lasc", subClass);
				sjk.put("match", source_title_sec_type);
				must1.add(sjk);
			}
			
		}
		
		if ((!frequency.equals("") && frequency != null && !frequency.equals("null") && !frequency.equals("ALL"))) {
			if(frequency.contains(",")){
				String[] split = frequency.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject source_title_sec_type = new JSONObject();
					source_title_sec_type.put("source_pub_frequency", s);
					sjk.put("match", source_title_sec_type);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject source_title_sec_type = new JSONObject();
				source_title_sec_type.put("source_pub_frequency", frequency);
				sjk.put("match", source_title_sec_type);
				must1.add(sjk);
			}
		}
		if ((!collectionType.equals("") && collectionType != null && !collectionType.equals("null") && !frequency.equals("ALL"))) {
			if(collectionType.contains(",")){
				String[] split = collectionType.split(",");
				for(String s : split){
					JSONObject sjk = new JSONObject();
					JSONObject source_title_sec_type = new JSONObject();
					source_title_sec_type.put("collection_type", s);
					sjk.put("match", source_title_sec_type);
					must1.add(sjk);
				}
			}else{
				JSONObject sjk = new JSONObject();
				JSONObject source_title_sec_type = new JSONObject();
				source_title_sec_type.put("collection_type", collectionType);
				sjk.put("match", source_title_sec_type);
				must1.add(sjk);
			}
		}
		if ((!initial.equals("") && initial != null && !initial.equals("null") && !initial.equals("ALL"))) {
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("source_source_initial", initial);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if ((!access_license.equals("") && access_license != null && !access_license.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("access_license-type", access_license);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		
		JSONObject is_display = new JSONObject();
		JSONObject is_displaymatch = new JSONObject();
		is_displaymatch.put("is_display", 1);
		is_display.put("match", is_displaymatch);
		must1.add(is_display);
		
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryPatentJsonGL(String value,String date,String ipcfl,String cpcfl,String gj,String type,String esi,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	

    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	fields.put("article_article-title_ik", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	/*JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("uuid",order1);
    	sort.add(uuid);*/
    	query.put("sort",sort);
    	
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	//must1.add(match_all);
    	
//    	JSONObject sources = new JSONObject();
//    	JSONObject regexp = new JSONObject();
//    	sources.put("source", ".*Patent");
//    	regexp.put("regexp", sources);
//    	must1.add(regexp);
    	if (value != null && !value.equals("") && !value.equals("null")) {
    	JSONObject multi_match = new JSONObject();
    	String[] arr = new String[]{
    			"article_article-title_ik^8",
    			"contrib_full-name-applicant_bg",
    			"contrib_full-name-inventor_bg",
    			"article_abstract_ik"};
//    	String[] arr = new String[]{
//    			"total_ik"};
    	//param.put("total_ik", value);
    	param.put("query", value);
    	param.put("operator", "and");
    	param.put("fields", arr);
    	//param.put("type", "best_fields");
    	multi_match.put("multi_match", param);
    	must1.add(multi_match);
    	}else{
        	match_all.put("match_all", param);
        	must1.add(match_all);
    	}
    	if (!date.equals("") && date != null && !date.equals("null")) {
    		if(!date.contains(",")){
    			JSONObject match = new JSONObject();
    			JSONObject dates = new JSONObject();
    			dates.put("article_issue-year_bg", date);
    			match.put("match", dates);
    			must1.add(match);
    		}else{
    			String[] split = date.split(",");
    			String starttime = "";
    			String endtime = "";
    			String lx = "";
    			if(split.length>2){
    				starttime = split[0];
    				endtime = split[1];
    				lx = split[2];
    			}
    			
    			JSONObject nulls = new JSONObject();
    			JSONObject range = new JSONObject();
    			JSONObject datet = new JSONObject();
    			JSONObject fromto = new JSONObject();
    			if(!starttime.equals("*") && !endtime.equals("*")){
    				fromto.put("from", starttime);
    				fromto.put("to", endtime);
    			}
    			if(!starttime.equals("*") && endtime.equals("*")){
    				fromto.put("from", starttime);
    				SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
    				String currentyear = sdf.format(new Date());
    				fromto.put("to", currentyear);
    			}
    			if(starttime.equals("*") && !endtime.equals("*")){
    				fromto.put("from", null);
    				fromto.put("to", endtime);
    			}
    			if(lx.equals("申请日")){
    				datet.put("article_app-year_bg", fromto);  
    			}
    			if(lx.equals("公开(公告)日")){
    				datet.put("article_issue-year_bg", fromto);  
    			}
    			range.put("range", datet);
    			must1.add(range);
    		}
			
		}
		if((!ipcfl.equals("") && ipcfl != null && !ipcfl.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("subj-class-kwd_classification-IPC-main_bg", ipcfl);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if((!cpcfl.equals("") && cpcfl != null && !cpcfl.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("subj-class-kwd_classification-CPC_bg", cpcfl);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if((!gj.equals("") && gj != null && !gj.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("contrib_country_country", gj);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if((!type.equals("") && type != null && !type.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("article_article-type", type);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		
		if((!esi.equals("") && esi != null && !esi.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("subj-class-kwd_classification_esi_bg", esi);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	JSONObject args = new JSONObject();
    	
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "article_issue-year_bg");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("article_issue-year_bg", p2);

    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "contrib_country_country");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("contrib_country_country", p4);
    	
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "subj-class-kwd_classification-IPC-main_bg");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("IPC-further_bg", p1);
    	
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "subj-class-kwd_classification-CPC_bg");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("classification-CPC_bg", p3);
    	
    	JSONObject p5 = new JSONObject();
    	JSONObject t5 = new JSONObject();
    	t5.put("field", "article_article-type");
    	t5.put("size", 10);
    	p5.put("terms", t5);
    	args.put("article_article-type", p5);
    	
    	JSONObject p6 = new JSONObject();
    	JSONObject t6 = new JSONObject();
    	t6.put("field", "subj-class-kwd_classification_esi_bg");
    	t6.put("size", 10);
    	p6.put("terms", t6);
    	args.put("subj-class-kwd_classification_esi_bg", p6);
    	
    	query.put("aggs",args);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    
    public static String createQqueryPatentJsonGLAgg(String value,String date,String inventor,String applicant,String gj,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	

    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	fields.put("article_article-title_ik", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("uuid",order1);
    	sort.add(uuid);
    	query.put("sort",sort);
    	
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	//must1.add(match_all);
    	
//    	JSONObject sources = new JSONObject();
//    	JSONObject regexp = new JSONObject();
//    	sources.put("source", ".*Patent");
//    	regexp.put("regexp", sources);
//    	must1.add(regexp);
    	if (value != null && !value.equals("") && !value.equals("null")) {
    	JSONObject multi_match = new JSONObject();
    	String[] arr = new String[]{
    			"article_article-title_ik",
    			"article_abstract_ik"};
//    	String[] arr = new String[]{
//    			"total_ik"};
    	//param.put("total_ik", value);
    	param.put("query", value);
    	param.put("operator", "and");
    	param.put("fields", arr);
    	//param.put("type", "best_fields");
    	multi_match.put("multi_match", param);
    	must1.add(multi_match);
    	}else{
        	match_all.put("match_all", param);
        	must1.add(match_all);
    	}
    	if (!date.equals("") && date != null && !date.equals("null")) {
//			JSONObject match = new JSONObject();
//			JSONObject dates = new JSONObject();
//			dates.put("article_year", date);
//			match.put("match", dates);
//			must1.add(match);
			String[] split = date.split(",");
			String starttime = "";
			String endtime = "";
			starttime = split[0];
			endtime = split[1];
			JSONObject nulls = new JSONObject();
			JSONObject range = new JSONObject();
			JSONObject datet = new JSONObject();
			JSONObject fromto = new JSONObject();
			if(!starttime.equals(" ") && !endtime.equals(" ")){
				fromto.put("from", starttime);
				fromto.put("to", endtime);
			}
			if(!starttime.equals(" ") && endtime.equals(" ")){
				fromto.put("from", starttime);
				fromto.put("to", null);
			}
			if(starttime.equals(" ") && !endtime.equals(" ")){
				fromto.put("from", null);
				fromto.put("to", endtime);
			}
    		datet.put("article_app-year_bg", fromto);  
    		range.put("range", datet);
    		must1.add(range);
		}
		if((!inventor.equals("") && inventor != null && !inventor.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("contrib_full-name-inventor_bg", inventor);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if((!applicant.equals("") && applicant != null && !applicant.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("contrib_full-name-applicant_bg", applicant);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if((!gj.equals("") && gj != null && !gj.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("contrib_country_country", gj);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	JSONObject args = new JSONObject();
//    	JSONObject p1 = new JSONObject();
//    	JSONObject t1 = new JSONObject();
//    	t1.put("field", "contrib_full-name-inventor_bg");
//    	t1.put("size", 10);
//    	p1.put("terms", t1);
//    	args.put("contrib_full-name-inventor_bg", p1);
//    	JSONObject p2 = new JSONObject();
//    	JSONObject t2 = new JSONObject();
//    	t2.put("field", "article_app-year_bg");
//    	t2.put("size", 10);
//    	p2.put("terms", t2);
//    	args.put("article_app-year_bg", p2);
//    	JSONObject p3 = new JSONObject();
//    	JSONObject t3 = new JSONObject();
//    	t3.put("field", "contrib_full-name-applicant_bg");
//    	t3.put("size", 10);
//    	p3.put("terms", t3);
//    	args.put("contrib_full-name-applicant_bg", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "contrib_country_country");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("contrib_country_country", p4);
    	query.put("aggs",args);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryInsJsonGL(String value,String research,String city,String gj,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	
    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	fields.put("contrib_institution", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("contrib_institution_research-domain",order1);
    	sort.add(uuid);
//    	JSONObject uuid1 = new JSONObject();
//    	JSONObject order2 = new JSONObject();
//    	order2.put("order", "desc");
//    	uuid1.put("uuid",order2);
//    	sort.add(uuid1);
    	query.put("sort",sort);
    	
    	
//    	match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
		if (value != null && !value.equals("") && !value.equals("null")) {

	    	JSONObject multi_match = new JSONObject();
	    	String[] arr = null;
	    	arr = new String[]{
	    			"contrib_institution_ik",
	                "contrib_institution_display_ik^200",
	                "contrib_institution_research-domain_ik",
	                "contrib_city_ik"};
	    	param.put("query", value);
	    	param.put("operator", "and");
	    	param.put("type", "cross_fields");
	    	param.put("fields", arr);
	    	multi_match.put("multi_match", param);
	    	must1.add(multi_match);
//			param.put("contrib_institution", ".*"+value+".*");
//	    	match_all.put("regexp", param);
//			must1.add(match_all);
		}else{
			match_all.put("match_all", param);
			must1.add(match_all);
		}
    	/*JSONObject sources = new JSONObject();
    	JSONObject regexp = new JSONObject();
    	sources.put("uuid", "zzzzzzzzzztestdata.*");
    	regexp.put("regexp", sources);
    	must1.add(regexp);*/
    	
		if ((!research.equals("") && research != null && !research.equals("null"))) {
			if(research.contains(",")){
				String[] split = research.split(",");
				for(String s : split){
					JSONObject yz = new JSONObject();
					JSONObject entity_type = new JSONObject();
					entity_type.put("contrib_institution_research-domain", s);
					yz.put("match", entity_type);
					must1.add(yz);
				}
			}else{
				JSONObject yz = new JSONObject();
				JSONObject entity_type = new JSONObject();
				entity_type.put("contrib_institution_research-domain", research);
				yz.put("match", entity_type);
				must1.add(yz);
			}
			
		}
		if ((!city.equals("") && city != null && !city.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("contrib_city_bg", city);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if ((!gj.equals("") && gj != null && !gj.equals("null"))) {
			JSONObject yz = new JSONObject();
			JSONObject entity_type = new JSONObject();
			entity_type.put("contrib_country_bg", gj);
			yz.put("match", entity_type);
			must1.add(yz);
		}
		JSONObject is_display = new JSONObject();
		JSONObject is_displaymatch = new JSONObject();
		is_displaymatch.put("is_display", 1);
		is_display.put("match", is_displaymatch);
		must1.add(is_display);
		
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
		
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "contrib_institution_research-domain");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("contrib_institution_research-domain", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "contrib_city_bg");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("contrib_city", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "contrib_country_bg");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("contrib_country", p3);
    	query.put("aggs",args);
    	
    	int pageSize = 10;
    	if(pageNo<=1){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryInsJsonGLAgg(String value,String research,String city,String gj,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	
    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	fields.put("contrib_institution", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("contrib_institution_research-domain",order1);
    	sort.add(uuid);
    	query.put("sort",sort);
    	
    	
//    	match_all = JSONObject.parseObject("{\"bool\": {\"must\": {\"bool\": {\"must_not\": {\"missing\": {\"field\": \"contrib_full-name\"}}}}}}");
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
		if (value != null && !value.equals("") && !value.equals("null")) {

//	    	JSONObject multi_match = new JSONObject();
//	    	String[] arr = null;
//	    	arr = new String[]{
//	    	              	"contrib_institution_ik"};
//	    	param.put("query", value);
//	    	param.put("operator", "and");
//	    	param.put("fields", arr);
//	    	//param.put("type", "best_fields");
//	    	multi_match.put("multi_match", param);
//	    	must1.add(multi_match);
			param.put("contrib_institution", ".*"+value+".*");
	    	match_all.put("regexp", param);
			must1.add(match_all);
		}else{
			match_all.put("match_all", param);
			must1.add(match_all);
		}
    	/*JSONObject sources = new JSONObject();
    	JSONObject regexp = new JSONObject();
    	sources.put("uuid", "zzzzzzzzzztestdata.*");
    	regexp.put("regexp", sources);
    	must1.add(regexp);*/
    	
		if ((!research.equals("") && research != null && !research.equals("null"))) {
			JSONObject yz = new JSONObject();
			JSONObject entity_type = new JSONObject();
			entity_type.put("contrib_institution_research-domain", research);
			yz.put("match", entity_type);
			must1.add(yz);
		}
		if ((!city.equals("") && city != null && !city.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject source_title_sec_type = new JSONObject();
			source_title_sec_type.put("contrib_institution_city", city);
			sjk.put("match", source_title_sec_type);
			must1.add(sjk);
		}
		if ((!gj.equals("") && gj != null && !gj.equals("null"))) {
			JSONObject yz = new JSONObject();
			JSONObject entity_type = new JSONObject();
			entity_type.put("contrib_institution_country", gj);
			yz.put("match", entity_type);
			must1.add(yz);
		}
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
		
    	
    	JSONObject args = new JSONObject();
//    	JSONObject p1 = new JSONObject();
//    	JSONObject t1 = new JSONObject();
//    	t1.put("field", "contrib_institution_research-domain");
//    	t1.put("size", 10);
//    	p1.put("terms", t1);
//    	args.put("contrib_institution_research-domain", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "contrib_institution_city");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("contrib_institution_city", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "contrib_institution_country");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("contrib_institution_country", p3);
    	query.put("aggs",args);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryExpertJson(String value,String subject,String ins,String role, String researchSubject,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	 
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("contrib_major",order1);
    	sort.add(uuid);
    	query.put("sort",sort);     
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "contrib_major");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("contrib_major", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "contrib_institution");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("contrib_institution", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "contrib_role");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("contrib_role", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "contrib_research-subject");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("contrib_research-subject", p4);
    	query.put("aggs",args);
    	
    	
//    	param = JSONObject.parseObject("{\"regexp\": {\"uuid\": \"zzzzzzzzzztestdata.*\"}}");
//    	query.put("filter", param);
    //	match_all = JSONObject.parseObject("{\"match_all\": {}}");
    	//query.put("query", match_all);
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	match_all = JSONObject.parseObject("{\"match_all\": {}}");
    	must1.add(match_all);
    	
//    	JSONObject sources = new JSONObject();
//    	JSONObject regexp = new JSONObject();
//    	sources.put("uuid", ".*zzzzzzzzzztestdata.*");
//    	regexp.put("regexp", sources);
//    	must1.add(regexp);
    	
		if ((!subject.equals("") && subject != null && !subject.equals("null"))) {
			JSONObject yz = new JSONObject();
			JSONObject contrib_major = new JSONObject();
			contrib_major.put("contrib_major", subject);
			yz.put("match", contrib_major);
			must1.add(yz);
		}
		if ((!ins.equals("") && ins != null && !ins.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("contrib_institution", ins);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
		}
		if ((!role.equals("") && role != null && !role.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("contrib_role", role);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
		}	
		if ((!researchSubject.equals("") && researchSubject != null && !researchSubject.equals("null"))) {
			JSONObject sjk = new JSONObject();
			JSONObject contrib_collab = new JSONObject();
			contrib_collab.put("contrib_research-subject", researchSubject);
			sjk.put("match", contrib_collab);
			must1.add(sjk);
		}
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    public static String createQqueryAllJson(){
    	JSONObject query = new JSONObject();
    	JSONObject param = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	match_all.put("match_all", param);
    	query.put("query", match_all);
//    	//System.out.println(query.toString());
    	return query.toString();
    }
    public static String createQqueryFundJson(String value,String year,String type,String ins,String author,String xmzz,int pageNo,String orderBy,String method){
    	JSONObject query = new JSONObject();
    	JSONObject match_all = new JSONObject();
    	JSONObject param = new JSONObject();
    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
    	//param.put("uuid", value);
    	match_all.put("match_all", param);
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	query.put("sort",sort);
    	JSONObject bool1 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	JSONObject bool2 = new JSONObject();
    	must1.add(match_all);
    	
		if((!year.equals("") && year != null && !year.equals("null"))){
			JSONObject yz = new JSONObject();
			JSONObject award_year = new JSONObject();
			award_year.put("award_year", year);
			yz.put("match", award_year);
			must1.add(yz);
		}
		if((!type.equals("") && type != null && !type.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject award_award_type = new JSONObject();
			award_award_type.put("award_award-type", type);
			sjk.put("match", award_award_type);
			must1.add(sjk);
		}
		if((!ins.equals("") && ins != null && !ins.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject award_award_type = new JSONObject();
			award_award_type.put("award_instituition", ins);
			sjk.put("match", award_award_type);
			must1.add(sjk);
		}
		if((!author.equals("") && author != null && !author.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject author1 = new JSONObject();
			author1.put("award_PI", author);
			sjk.put("match", author1);
			must1.add(sjk);
		}
		if((!xmzz.equals("") && xmzz != null && !xmzz.equals("null"))){
			JSONObject sjk = new JSONObject();
			JSONObject author1 = new JSONObject();
			author1.put("award_funding_institution", xmzz);
			sjk.put("match", author1);
			must1.add(sjk);
		}
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "award_award-type");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("award_award-type", p1);
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "award_year");
    	t2.put("size", 10);
    	p2.put("terms", t2);
    	args.put("award_year", p2);
    	JSONObject p3 = new JSONObject();
    	JSONObject t3 = new JSONObject();
    	t3.put("field", "award_instituition");
    	t3.put("size", 10);
    	p3.put("terms", t3);
    	args.put("award_instituition", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "award_PI");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("award_PI", p4);
    	JSONObject p5 = new JSONObject();
    	JSONObject t5 = new JSONObject();
    	t5.put("field", "award_funding_institution");
    	t5.put("size", 10);
    	p5.put("terms", t5);
    	args.put("award_funding_institution", p5);
    	query.put("aggs",args);
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 2;
    	}
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    
public static String createQqueryJsonPagePaper(String value,String uuid,int pageNo){
    	
    	JSONObject query = new JSONObject();
    	JSONObject args = new JSONObject();
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONObject param = new JSONObject();
		JSONArray must1 = new JSONArray();
    	if((!value.equals("") && value != null && !value.equals("null"))){
    		
        	JSONObject multi_match = new JSONObject();
        	String[] arr = new String[]{
        			"json.subjClassKwd.subj-class-kwd_kwd^8",
					"article_article-title_ik^5",
					"source_source-title_ik^5",
					"contrib_full-name_ik^5",
	              	"article_abstract_ik"};
        	
        	param.put("query", value);
        	param.put("operator", "and");
        	param.put("fields", arr);
        //	param.put("type", "best_fields");
        	multi_match.put("multi_match", param);
        	must1.add(multi_match);
        	
        	
        	
    	}
//    	JSONObject sources = new JSONObject();
//    	JSONObject regexp = new JSONObject();
//      sources.put("source", ".*Article");
//    	regexp.put("regexp", sources);
//    	must1.add(regexp);
    	bool2.put("must", must1);
    	JSONObject must_not = new JSONObject();
    	JSONObject term = new JSONObject();
    	must_not.put("term", term);
    	term.put("uuid", uuid);
    	bool2.put("must_not",must_not);
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", "desc");
    	_score.put("_score",order);
    	sort.add(_score);
//    	JSONObject uuids = new JSONObject();
//    	JSONObject order1 = new JSONObject();
//    	order1.put("order", "desc");
//    	uuids.put("uuid",order1);
//    	sort.add(uuids);
    	query.put("sort",sort);
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    
    
    public static String createQqueryJsonJiJinxg(String value,String uuid,String awardSubjectClassxg,String awardKeywordxg,int pageNo){
    	
    	JSONObject query = new JSONObject();
    	JSONObject args = new JSONObject();
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray mustarr = new JSONArray();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool4 = new JSONObject();
    	JSONArray should = new JSONArray();
    	JSONArray must1 = new JSONArray();
    	if((!awardSubjectClassxg.equals("") && awardSubjectClassxg != null && !awardSubjectClassxg.equals("null"))){
    		String[] xk = awardSubjectClassxg.split(",");
    		for(String s : xk){
	    		JSONObject term = new JSONObject();
	        	JSONObject param = new JSONObject();
	        	param.put("award_subjectClass", s);
	        	term.put("match", param);
	        	should.add(term);
	    	}
    	}
    	if((!awardKeywordxg.equals("") && awardKeywordxg != null && !awardKeywordxg.equals("null"))){
    		String[] gjc = awardKeywordxg.split(",");
    		for(String s : gjc){
	    		JSONObject term = new JSONObject();
	        	JSONObject param = new JSONObject();
	        	param.put("award_keyword-cn", s);
	        	term.put("match", param);
	        	should.add(term);
	    	}
    	}
    	bool2.put("should", should);
    	
    	JSONObject must_not = new JSONObject();
    	JSONObject term = new JSONObject();
    	must_not.put("term", term);
    	term.put("uuid", uuid);
    	bool2.put("must_not",must_not);
    	
    	bool4.put("bool", bool2);
    	mustarr.add(bool4);
    	if((!value.equals("") && value != null && !value.equals("null"))){
	    	JSONObject match_all = new JSONObject();
	    	JSONObject param = new JSONObject();
	    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
	    	param.put("award_award-name_ik", value);
	    	match_all.put("match", param);
	    	mustarr.add(match_all);
    	}
    	JSONObject is_display = new JSONObject();
		JSONObject is_displaymatch = new JSONObject();
		is_displaymatch.put("is_display", 1);
		is_display.put("match", is_displaymatch);
		mustarr.add(is_display);
		
		bool3.put("must", mustarr);
    	
    	
    	bool1.put("bool", bool3);
    	query.put("query", bool1);
    	
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    
    
public static String createQqueryJsonPaperJiJinInsExper(String uuid,String stkgjc,String kwdIK,String st1,String st2,String st3,String st4,int dis){
    	
    	JSONObject query = new JSONObject();
    	JSONObject args = new JSONObject();
    	
    	int pageSize = 10;
    	
    	
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray mustarr = new JSONArray();
    	JSONObject bool3 = new JSONObject();
    	JSONObject must = new JSONObject();
    	JSONObject bool4 = new JSONObject();
    	JSONArray should = new JSONArray();
    	JSONArray must1 = new JSONArray();
    	if((!stkgjc.equals("") && stkgjc != null && !stkgjc.equals("null"))){
    		String[] xk = stkgjc.split(",");
    		if(xk.length>0){
    			if(!st1.equals("")){
    				for(String s : xk){
    		    		JSONObject term = new JSONObject();
    		        	JSONObject param = new JSONObject();
    		        	param.put(st1, s);
    		        	term.put("match", param);
    		        	should.add(term);
    		    	}
    			}
	    		
	    		if(!st2.equals("")){
	    			for(String s : xk){
	    				JSONObject term = new JSONObject();
	    				JSONObject param = new JSONObject();
	    				param.put(st2, s);
	    				term.put("match", param);
	    				should.add(term);
	    			}
	    		}
	    		if(!st3.equals("")){
	    			for(String s : xk){
	    				JSONObject term = new JSONObject();
	    				JSONObject param = new JSONObject();
	    				param.put(st3, s);
	    				term.put("match", param);
	    				should.add(term);
	    			}
	    		}
	    		if(!st4.equals("")){
	    			for(String s : xk){
	    				JSONObject term = new JSONObject();
	    				JSONObject param = new JSONObject();
	    				param.put(st4, s);
	    				term.put("match", param);
	    				should.add(term);
	    			}
	    		}
    		}
    	}
    	
    	if((!kwdIK.equals("") && kwdIK != null && !kwdIK.equals("null"))){
    		String[] xk = kwdIK.split(",");
    		if(xk.length>0){
    			if(!st1.equals("")){
    				for(String s : xk){
    					JSONObject term = new JSONObject();
    					JSONObject param = new JSONObject();
    					param.put(st1, s);
    					term.put("match", param);
    					should.add(term);
    				}
    			}
    			if(!st2.equals("")){
    				for(String s : xk){
    					JSONObject term = new JSONObject();
    					JSONObject param = new JSONObject();
    					param.put(st2, s);
    					term.put("match", param);
    					should.add(term);
    				}
    			}
    			if(!st3.equals("")){
    				for(String s : xk){
    					JSONObject term = new JSONObject();
    					JSONObject param = new JSONObject();
    					param.put(st3, s);
    					term.put("match", param);
    					should.add(term);
    				}
    			}
    			if(!st4.equals("")){
    				for(String s : xk){
    					JSONObject term = new JSONObject();
    					JSONObject param = new JSONObject();
    					param.put(st4, s);
    					term.put("match", param);
    					should.add(term);
    				}
    			}
    		}
    	}
    	
    	
    	bool2.put("should", should);
    	
    	JSONObject must_not = new JSONObject();
    	JSONObject term = new JSONObject();
    	must_not.put("term", term);
    	term.put("uuid", uuid);
    	bool2.put("must_not",must_not);
    	
    	bool4.put("bool", bool2);
    	mustarr.add(bool4);
    	
    	if(dis == 1){
    		JSONObject is_display = new JSONObject();
    		JSONObject is_displaymatch = new JSONObject();
    		is_displaymatch.put("is_display", 1);
    		is_display.put("match", is_displaymatch);
    		mustarr.add(is_display);
    	}
		
		bool3.put("must", mustarr);
    	
    	
    	bool1.put("bool", bool3);
    	query.put("query", bool1);
    	
    	query.put("from",1);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
public static String createQqueryJsonPage(String value,String uuid,int pageNo,int pageSize){
    	
    	JSONObject query = new JSONObject();
    	JSONObject args = new JSONObject();
    	
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	if((!value.equals("") && value != null && !value.equals("null"))){
	    	JSONArray must1 = new JSONArray();
	    	JSONObject match_all = new JSONObject();
	    	JSONObject param = new JSONObject();
	    	//value="8bc8ad0d53d5b8499ec64a4a88c1ad08";
	    	param.put("award_award-name_ik", value);
	    	match_all.put("match", param);
	    	must1.add(match_all);
	    	bool2.put("must",must1);
    	}
    	JSONObject must_not = new JSONObject();
    	JSONObject term = new JSONObject();
    	must_not.put("term", term);
    	term.put("uuid", uuid);
    	bool2.put("must_not",must_not);
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    //生成分页的查询json

    public static String createQqueryJsonPage(String value,int pageNo,String source,String orderBy,String method){
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	JSONObject match_all = new JSONObject();
    	JSONObject query = new JSONObject();
    	JSONObject param = new JSONObject();
    	
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	
		if (value != null && !value.equals("") && !value.equals("null")) {
			JSONObject multi_match = new JSONObject();
	    	String[] arr = null;
//	    	if(value.equals("人工智能")){
//	    	arr = new String[]{
//	    			"article_article-title-en_ik",
//	    			"article_abstract-en_ik",
//	    	        "subj-class-kwd_kwd-group_kwd-en_ik",
//	    	        };
//	    	}
	    		arr = new String[]{
	    					"article_article-title_ik",
	    	              	"article_abstract_ik"};
	    	
	    	param.put("query", value);
	    	param.put("operator", "and");
	    	param.put("fields", arr);
	    	//param.put("type", "best_fields");
	    	multi_match.put("multi_match", param);
	    	must1.add(multi_match);
		}else{
			match_all.put("match_all", param);
	    	must1.add(match_all);
		}
		JSONObject args = new JSONObject();
		JSONObject p5 = new JSONObject();
    	JSONObject t5 = new JSONObject();
    	t5.put("field", "json.subjClassKwd.subj-class-kwd_kwd");
    	t5.put("size", 10);
    	p5.put("terms", t5);
    	args.put("subj-class-kwd_kwd", p5);
    	query.put("aggs",args);
    	
    	JSONObject sources = new JSONObject();
    	JSONObject regexp = new JSONObject();
    	sources.put("source", ".*Article");
    	regexp.put("regexp", sources);
    	must1.add(regexp);
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("article_abstract",order1);
    	sort.add(uuid);
    	query.put("sort",sort);
    	
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
public static String createQueryJsonImplPaper(String value,String year,int pageNo,int pageSize,String source,String orderBy,String method){
    	
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	JSONObject match_all = new JSONObject();
    	JSONObject query = new JSONObject();
    	JSONObject param = new JSONObject();
    	
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	JSONArray must1 = new JSONArray();
    	
		if (value != null && !value.equals("") && !value.equals("null")) {
			JSONObject multi_match = new JSONObject();
	    	String[] arr = null;
	    		arr = new String[]{
	    					"article_article-title_ik",
	    					"json.subjClassKwd.subj-class-kwd_kwd",
	    	              	"article_abstract_ik"};
	    	
	    	param.put("query", value);
	    	param.put("operator", "and");
	    	param.put("fields", arr);
	    	//param.put("type", "best_fields");
	    	multi_match.put("multi_match", param);
	    	must1.add(multi_match);
		}else{
			match_all.put("match_all", param);
	    	must1.add(match_all);
		}
		JSONObject args = new JSONObject();
    	JSONObject p5 = new JSONObject();
    	JSONObject t5 = new JSONObject();
    	t5.put("field", "article_year");
    	t5.put("size", 10);
    	p5.put("terms", t5);
    	args.put("article_year", p5);
    	query.put("aggs",args);
    	
    	JSONObject sources = new JSONObject();
    	JSONObject regexp = new JSONObject();
    	sources.put("source", ".*Article");
    	regexp.put("regexp", sources);
    	must1.add(regexp);
    	
    	if (!year.equals("") && year != null && !year.equals("null")) {
			JSONObject match = new JSONObject();
			JSONObject dates = new JSONObject();
			dates.put("article_year", year);
			match.put("match", dates);
			must1.add(match);
		}
    	
    	bool2.put("must",must1);
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	JSONArray sort = new JSONArray();
    	
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuid = new JSONObject();
    	JSONObject order1 = new JSONObject();
    	order1.put("order", "desc");
    	uuid.put("uuid",order1);
    	sort.add(uuid);
    	query.put("sort",sort);
    	
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
public static String createQueryJsonImplPatent(String value,String year,int pageNo,int pageSize,String source,String orderBy,String method){
	
	if(pageNo<=0){
		pageNo = 1;
	}
	JSONObject match_all = new JSONObject();
	JSONObject query = new JSONObject();
	JSONObject param = new JSONObject();
	
	JSONObject bool1 = new JSONObject();
	JSONObject bool2 = new JSONObject();
	JSONArray must1 = new JSONArray();
	
	if (value != null && !value.equals("") && !value.equals("null")) {
		JSONObject multi_match = new JSONObject();
    	String[] arr = null;
    		arr = new String[]{
    					"article_article-title_ik",
    	              	"article_abstract_ik"};
    	
    	param.put("query", value);
    	param.put("operator", "and");
    	param.put("fields", arr);
    	//param.put("type", "best_fields");
    	multi_match.put("multi_match", param);
    	must1.add(multi_match);
	}else{
		match_all.put("match_all", param);
    	must1.add(match_all);
	}
	JSONObject args = new JSONObject();
	JSONObject p5 = new JSONObject();
	JSONObject t5 = new JSONObject();
	t5.put("field", "article_app-year_bg");
	t5.put("size", 10);
	p5.put("terms", t5);
	args.put("article_app-year_bg", p5);
	query.put("aggs",args);
	
	
	if (!year.equals("") && year != null && !year.equals("null")) {
		JSONObject match = new JSONObject();
		JSONObject dates = new JSONObject();
		dates.put("article_app-year_bg", year);
		match.put("match", dates);
		must1.add(match);
	}
	
	bool2.put("must",must1);
	bool1.put("bool", bool2);
	query.put("query", bool1);
	JSONArray sort = new JSONArray();
	
	JSONObject _score = new JSONObject();
	JSONObject order = new JSONObject();
	order.put("order", method);
	_score.put(orderBy,order);
	sort.add(_score);
	JSONObject uuid = new JSONObject();
	JSONObject order1 = new JSONObject();
	order1.put("order", "desc");
	uuid.put("uuid",order1);
	sort.add(uuid);
	query.put("sort",sort);
	
	query.put("from",(pageNo-1)*pageSize);
	query.put("size", pageSize);
	//System.out.println(query.toString());
	return query.toString();
	
}
public static String createQueryJsonImplProject(String value,String year,int pageNo,int pageSize,String orderBy,String method){
	
	if(pageNo<=0){
		pageNo = 1;
	}
	JSONObject match_all = new JSONObject();
	JSONObject query = new JSONObject();
	JSONObject param = new JSONObject();
	
	JSONObject bool1 = new JSONObject();
	JSONObject bool2 = new JSONObject();
	JSONArray must1 = new JSONArray();
	
	if (value != null && !value.equals("") && !value.equals("null")) {
		JSONObject multi_match = new JSONObject();
    	String[] arr = null;
    		arr = new String[]{
    				"award_award-name_ik",
    				"award_apply_abstraction_ik",
    					"award_keyword-cn_ik"
    	              	};
    	
    	param.put("query", value);
    	param.put("operator", "and");
    	param.put("fields", arr);
    	//param.put("type", "best_fields");
    	multi_match.put("multi_match", param);
    	must1.add(multi_match);
	}else{
		match_all.put("match_all", param);
    	must1.add(match_all);
	}
	JSONObject args = new JSONObject();
	JSONObject p5 = new JSONObject();
	JSONObject t5 = new JSONObject();
	t5.put("field", "award_year");
	t5.put("size", 10);
	p5.put("terms", t5);
	args.put("award_year", p5);
	query.put("aggs",args);

	if (!year.equals("") && year != null && !year.equals("null")) {
		JSONObject match = new JSONObject();
		JSONObject dates = new JSONObject();
		dates.put("award_year", year);
		match.put("match", dates);
		must1.add(match);
	}
	
	bool2.put("must",must1);
	bool1.put("bool", bool2);
	query.put("query", bool1);
	JSONArray sort = new JSONArray();
	
	JSONObject _score = new JSONObject();
	JSONObject order = new JSONObject();
	order.put("order", method);
	_score.put(orderBy,order);
	sort.add(_score);
	JSONObject uuid = new JSONObject();
	JSONObject order1 = new JSONObject();
	order1.put("order", "desc");
	uuid.put("uuid",order1);
	sort.add(uuid);
	query.put("sort",sort);
	
	query.put("from",(pageNo-1)*pageSize);
	query.put("size", pageSize);
	//System.out.println(query.toString());
	return query.toString();
	
}
    	public static String createQqueryJsonPage(String value,List<String> list,String author,String date,String jou,String ins,String sub,String lwlx,int pageNo,String source,String orderBy,String method){
    	
    	int pageSize = 10;
    	if(pageNo<=0){
    		pageNo = 1;
    	}
    	JSONObject match_all = new JSONObject();
    	JSONObject query = new JSONObject();
    	
    	
    	JSONObject bool1 = new JSONObject();
    	JSONObject bool2 = new JSONObject();
    	
    	
    	JSONArray must1 = new JSONArray();
    	
    	
		if (value != null && !value.equals("") && !value.equals("null")) {
			JSONObject bool4 = new JSONObject();
	    	JSONObject bool3 = new JSONObject();
	    	JSONArray should = new JSONArray();
	    	for(String str:list){
	    		JSONObject param = new JSONObject();
	    		JSONObject multi_match = new JSONObject();
		    	String[] arr = null;
		    	arr = new String[]{
		    			"json.subjClassKwd.subj-class-kwd_kwd^50",
						"article_article-title_ik^15",
						"contrib_full-name_ik^1",
						"contrib_full-name^8",
		              	"article_abstract_ik"};
		    	param.put("query", str);
		    	param.put("operator", "and");
		    	param.put("type", "cross_fields");
		    	param.put("fields", arr);
		    	//param.put("type", "best_fields");
		    	multi_match.put("multi_match", param);
		    	should.add(multi_match);
	    	}
	    	bool4.put("should", should);
		    bool3.put("bool", bool4);
		    must1.add(bool3);
		}else{
			JSONObject param = new JSONObject();
			match_all.put("match_all", param);
	    	must1.add(match_all);
		}
    	
    	
//    	JSONObject sources = new JSONObject();
//    	JSONObject regexp = new JSONObject();
//    	sources.put("source", ".*Article");
//    	regexp.put("regexp", sources);
//    	must1.add(regexp);
    	
    	if (!author.equals("") && author != null && !author.equals("null")) {
			JSONObject match = new JSONObject();
			JSONObject uuid = new JSONObject();
			uuid.put("source", author);
			match.put("match", uuid);
			must1.add(match);
		}
		if (!date.equals("") && date != null && !date.equals("null")) {
//			
			if(date.contains(",")){
				String[] split = date.split(",");
				String starttime = "";
				String endtime = "";
				starttime = split[0];
				endtime = split[1];
				JSONObject nulls = new JSONObject();
				JSONObject range = new JSONObject();
				JSONObject datet = new JSONObject();
				JSONObject fromto = new JSONObject();
				if(!starttime.equals("*") && !endtime.equals("*")){
					fromto.put("from", starttime);
					fromto.put("to", endtime);
				}
				if(!starttime.equals("*") && endtime.equals("*")){
					fromto.put("from", starttime);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
					String currentyear = sdf.format(new Date());
					fromto.put("to", currentyear);
				}
				if(starttime.equals("*") && !endtime.equals("*")){
					fromto.put("from", null);
					fromto.put("to", endtime);
				}
	    		datet.put("article_year", fromto);  
	    		range.put("range", datet);
	    		must1.add(range);
			}else{
				JSONObject match = new JSONObject();
				JSONObject dates = new JSONObject();
				dates.put("article_year", date);
				match.put("match", dates);
				must1.add(match);
			}
			
		}
		if (!jou.equals("") && jou != null && !jou.equals("null")) {
	    	if(jou.contains(",")){
				String[] split = jou.split(",");
				for(String s : split){
					JSONObject match = new JSONObject();
					JSONObject jous = new JSONObject();
					jous.put("source_source-title", s);
					match.put("match", jous);
					must1.add(match);
				}
	    	}else{
	    		JSONObject match = new JSONObject();
	    		JSONObject jous = new JSONObject();
	    		jous.put("source_source-title", jou);
	    		match.put("match", jous);
	    		must1.add(match);
	    	}
			
		}
		if (!ins.equals("") && ins != null && !ins.equals("null")) {
			JSONObject match = new JSONObject();
			JSONObject inss = new JSONObject();
			inss.put("contrib_institution", ins);
			match.put("match", inss);
			must1.add(match);
		}
		if (!sub.equals("") && sub != null && !sub.equals("null")) {
			if(sub.contains(",")){
				String[] split = sub.split(",");
				for(String s : split){
					JSONObject match = new JSONObject();
					JSONObject subs = new JSONObject();
					subs.put("json.subjClassKwd.subj-class-kwd_kwd",s);
					match.put("match", subs);
					must1.add(match);
				}
			}else{
				JSONObject match = new JSONObject();
				JSONObject subs = new JSONObject();
				subs.put("json.subjClassKwd.subj-class-kwd_kwd", sub);
				match.put("match", subs);
				must1.add(match);
			}
			
			
			
		}
		if (!lwlx.equals("") && lwlx != null && !lwlx.equals("null")) {
	    	if(lwlx.contains(",")){
				String[] split = lwlx.split(",");
				for(String s : split){
					JSONObject match = new JSONObject();
					JSONObject subs = new JSONObject();
					subs.put("article_article-type-display", s);
					match.put("match", subs);
					must1.add(match);
				}
	    	}else{
	    		JSONObject match = new JSONObject();
				JSONObject subs = new JSONObject();
				subs.put("article_article-type-display", lwlx);
				match.put("match", subs);
				must1.add(match);
	    	}
			
			
		}
		
    	bool2.put("must",must1);
    	
    	/*JSONObject must_not = new JSONObject();
    	JSONObject terms = new JSONObject();
    	terms.put("source", ".*Patent");
    	must_not.put("regexp", terms);
    	bool2.put("must_not",must_not);*/
    	
    	
    	bool1.put("bool", bool2);
    	query.put("query", bool1);
    	
    	JSONObject args = new JSONObject();
    	JSONObject p2 = new JSONObject();
    	JSONObject t2 = new JSONObject();
    	t2.put("field", "article_year");
    	t2.put("size", 15);
    	JSONObject temsorder = new JSONObject();
    	temsorder.put("_term", "desc");
    	t2.put("order", temsorder);
    	p2.put("terms", t2);
    	args.put("article_year", p2);
    	JSONObject p1 = new JSONObject();
    	JSONObject t1 = new JSONObject();
    	t1.put("field", "article_article-type-display");
    	t1.put("size", 10);
    	p1.put("terms", t1);
    	args.put("article_article-type", p1);
    	
//    	JSONObject p3 = new JSONObject();
//    	JSONObject t3 = new JSONObject();
//    	t3.put("field", "contrib_institution");
//    	t3.put("size", 10);
//    	p3.put("terms", t3);
//    	args.put("contrib_institution", p3);
    	JSONObject p4 = new JSONObject();
    	JSONObject t4 = new JSONObject();
    	t4.put("field", "source_source-title");
    	t4.put("size", 10);
    	p4.put("terms", t4);
    	args.put("source_source-title", p4);
    	JSONObject p5 = new JSONObject();
    	JSONObject t5 = new JSONObject();
    	t5.put("field", "json.subjClassKwd.subj-class-kwd_kwd");
    	t5.put("size", 30);
    	t5.put("execution_hint", "map");
    	t5.put("collect_mode", "breadth_first");
    	p5.put("terms", t5);
    	args.put("subj-class-kwd_kwd", p5);
//    	JSONObject p6 = new JSONObject();
//    	JSONObject t6 = new JSONObject();
//    	t6.put("field", "source");
//    	t6.put("size", 10);
//    	p6.put("terms", t6);
//    	args.put("source", p6);
    	query.put("aggs",args);
    	
    	JSONObject highlight = new JSONObject();
    	JSONObject fields = new JSONObject();
    	JSONObject ss = new JSONObject();
    	/*if(source.equals("Patent")){
    		fields.put("article_article-title_ik", ss);
    	}*/
    	fields.put("article_article-title_ik", ss);
    	highlight.put("fields", fields);
    	query.put("highlight", highlight);
    	
    	JSONArray sort = new JSONArray();
    	JSONObject _score = new JSONObject();
    	JSONObject order = new JSONObject();
    	order.put("order", method);
    	_score.put(orderBy,order);
    	sort.add(_score);
    	JSONObject uuids = new JSONObject();
    	JSONObject order1s = new JSONObject();
    	order1s.put("order", "desc");
    	uuids.put("article_year",order1s);
    	sort.add(uuids);
    	query.put("sort",sort);
    	
    	query.put("from",(pageNo-1)*pageSize);
    	query.put("size", pageSize);
    	//System.out.println(query.toString());
    	return query.toString();
    	
    }
    	//q, plist, year, institution, lanmu, pageIndex
    	public static String jcreateQqueryJsonPage(String q,String year,String institution,String lanmu,Integer pageIndex){
        	
        	int pageSize = 10;
        	
        	JSONObject match_all = new JSONObject();
        	JSONObject query = new JSONObject();
        	
        	
        	JSONObject bool1 = new JSONObject();
        	JSONObject bool2 = new JSONObject();
        	JSONObject bool4 = new JSONObject();
        	JSONObject bool3 = new JSONObject();
        	JSONArray should = new JSONArray();
        	JSONObject multi_match = new JSONObject();
        	
        	
        	JSONArray must = new JSONArray();
        	
        	JSONObject param = new JSONObject();
        	String[] arr =  new String[]{"title","description"};
        	
    		if (q != null && !q.equals("") && !q.equals("null")) {
    	    	
		    	param.put("query", q);
		    	param.put("operator", "and");
		    	param.put("type", "cross_fields");
		    	param.put("fields", arr);
		    	//param.put("type", "best_fields");
		    	multi_match.put("multi_match", param);
		    	should.add(multi_match);
    	    	
    	    	bool4.put("should", should);
    		    bool3.put("bool", bool4);
    		    must.add(bool3);
    		}else{
    			match_all.put("match_all", param);
    	    	must.add(match_all);
    		}
        	
        	if (lanmu != null && !lanmu.equals("") && !lanmu.equals("null")) {
    			JSONObject match = new JSONObject();
    			JSONObject uuid = new JSONObject();
    			uuid.put("lanmu", lanmu);
    			match.put("match", uuid);
    			must.add(match);
    		}
    		
    		if (institution != null  && !institution.equals("") && !institution.equals("null")) {
    	    	if(institution.contains(",")){
    				String[] split = institution.split(",");
    				for(String s : split){
    					JSONObject match = new JSONObject();
    					JSONObject jous = new JSONObject();
    					jous.put("institution", s);
    					match.put("match", jous);
    					must.add(match);
    				}
    	    	}else{
    	    		JSONObject match = new JSONObject();
    	    		JSONObject jous = new JSONObject();
    	    		jous.put("institution", institution);
    	    		match.put("match", jous);
    	    		must.add(match);
    	    	}
    			
    		}
    		
        	bool2.put("must",must);
        	
        	bool1.put("bool", bool2);
        	query.put("query", bool1);
        	
        	JSONObject args = new JSONObject();
        	JSONObject p2 = new JSONObject();
        	JSONObject t2 = new JSONObject();
        	t2.put("field", "lanmu");
        	t2.put("size", 15);
        	p2.put("terms", t2);
        	args.put("lanmu", p2);
        	JSONObject p1 = new JSONObject();
        	JSONObject t1 = new JSONObject();
        	t1.put("field", "institution");
        	t1.put("size", 10);
        	p1.put("terms", t1);
        	args.put("institution", p1);
        	
        	query.put("aggs",args);
        	
//        	JSONArray sort = new JSONArray();
//        	JSONObject _score = new JSONObject();
//        	JSONObject order = new JSONObject();
//        	order.put("order", "desc");//method=desc
//        	_score.put("_score",order);//orderby=_score
//        	sort.add(_score);
//        	JSONObject uuids = new JSONObject();
//        	JSONObject order1s = new JSONObject();
//        	order1s.put("order", "desc");
//        	uuids.put("",order1s);
//        	sort.add(uuids);
//        	query.put("sort",sort);
        	
        	query.put("from",pageIndex);
        	query.put("size", pageSize);
        	System.out.println("-- " + query.toString());
        	return query.toString();
        	
        }
    
   
    
}
