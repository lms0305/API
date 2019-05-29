package com.qhit.utils.solr;
//                         _ooOoo_
//                        o8888888o    
//                        88" . "88    
//                        (| -_- |)    
//                         O\ = /O    
//                     ____/`---'\____    
//                   .   ' \\| |// `.    
//                    / \\||| : |||// \    
//                  / _||||| -:- |||||- \    
//                    | | \\\ - /// | |    
//                  | \_| ''\---/'' | |    
//                   \ .-\__ `-` ___/-. /    
//                ___`. .' /--.--\ `. . __    
//             ."" '< `.___\_<|>_/___.' >'"".    
//            | | : `- \`.;`\ _ /`;.`/ - ` : | |    
//              \ \ `-. \_ __\ /__ _/ .-` / /    
//      ======`-.____`-.___\_____/___.-`____.-'======    
//                         `=---='    
//  
//      .............................................    
//               佛祖保佑             永无BUG   
//       佛曰:    
//               写字楼里写字间，写字间里程序员；    
//               程序人员写程序，又拿程序换酒钱。    
//               酒醒只在网上坐，酒醉还来网下眠；    
//               酒醉酒醒日复日，网上网下年复年。    
//               但愿老死电脑间，不愿鞠躬老板前；    
//               奔驰宝马贵者趣，公交自行程序员。    
//               别人笑我忒疯癫，我笑自己命太贱；    
//               不见满街漂亮妹，哪个归得程序员？   

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.qhit.goodsInfo.pojo.GoodsInfo;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;
@Component
public class SolrUtil {

    //solr服务器所在的地址，core0为自己创建的文档库目录
    private final static String SOLR_URL = "http://172.25.20.124:8081/solr/goodsinfo";

    private static SolrServer solrServer = null;

    public SolrUtil() {

    }
    public static SolrServer getSolrServer(){
        if (solrServer==null){
            solrServer = new HttpSolrServer(SOLR_URL);
        }
        return  solrServer;
    }


    /**
     * 往索引库添加文档
     *
     * @throws SolrServerException
     * @throws IOException
     */
    public void insertOrUpdate(Object object){
        SolrInputDocument document = SolrTypeCast.toSolrInputDocument(object);
        SolrServer solrServer = getSolrServer();
        UpdateResponse add = null;
        try {
            add = solrServer.add(document);
            solrServer.commit();
            System.out.println("添加成功!||更新成功!");
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除索引
     * @throws IOException
     * @throws SolrServerException
     */
    public void delete(Object idValue){
        SolrServer solrServer = getSolrServer();
        try {
            UpdateResponse updateResponse = solrServer.deleteById(""+idValue);//根据id删除
            solrServer.commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        solrServer.deleteByQuery("product_keywords:教程");//自动查询条件删除

    }

    /**
     * 更新
     * @param object
     */
    public void update(Object object){
        try {
            SolrServer solrServer = getSolrServer();
            SolrInputDocument doc = new SolrInputDocument();
            Class<?> clazz = object.getClass();
            Field[] fields = clazz.getFields();
            for(Field field:fields){
                Object value = field.get(object);
                if (value!=null && value!=""){
                    field.setAccessible(true);
                    String name = field.getName();
                    doc.addField(name,value);
                }
            }
            UpdateResponse rsp = solrServer.add(doc);
            solrServer.commit();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 得到查询的总数量
     * @param column
     * @param searchContent
     * @return
     */
    public Integer searchCount(String column,String searchContent){
        SolrServer solrServer = getSolrServer();
        SolrQuery query = new SolrQuery(); // 查询对象
        // 搜索product_keywords域，product_keywords是复制域包括product_name和product_description
        // 设置商品分类、关键字查询
        // query.set("q", "product_keywords:挂钩 AND  product_catalog_name:幽默杂货");
        if (searchContent==null || "".equals(searchContent)){
            searchContent="*";
        }
        query.set("q",column+":"+searchContent);
        query.setStart(0);
        query.setRows(0);
        QueryResponse response = null; // 请求查询
        int count = 0;
        try {
            QueryResponse query2 = solrServer.query(query, SolrRequest.METHOD.POST);
            count = (int)query2.getResults().getNumFound();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 分页查询
     * @param column
     * @param searchContent
     * @param clazz
     * @param currentPage
     * @param pageSize
     * @return
     */
    public List searchByPage(String column,String searchContent,Class clazz,Integer currentPage,Integer pageSize){
        SolrServer solrServer = getSolrServer();
        SolrQuery query = new SolrQuery(); // 查询对象
        // 搜索product_keywords域，product_keywords是复制域包括product_name和product_description
        // 设置商品分类、关键字查询
        // query.set("q", "product_keywords:挂钩 AND  product_catalog_name:幽默杂货");
        if (searchContent==null || "".equals(searchContent)){
            searchContent="*";
        }
        query.set("q",column+":"+searchContent);
        query.setStart((currentPage-1)*pageSize);
        query.setRows(pageSize);
//        query.set("fq", "product_price:[1 TO 20]"); // 设置价格范围
        // 查询结果按照价格降序排序
        // query.set("sort", "product_price desc");
//        query.addSort("marketprice", SolrQuery.ORDER.desc);

        QueryResponse response = null; // 请求查询
        try {
            response = solrServer.query(query);
            SolrDocumentList docs = response.getResults(); // 查询结果
//            System.out.println("查询文档总数" + docs.getNumFound()); // 查询文档总数
            List list = new LinkedList();
            for (SolrDocument doc : docs) {
                Object o = SolrTypeCast.toBean(doc, clazz);
                list.add(o);
            }
            return list;
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SolrDocumentList testSearch3() throws SolrServerException {
        SolrServer solrServer = getSolrServer();
        SolrQuery query = new SolrQuery();
        query.setQuery("product_keywords:透明挂钩 "); // 设置商品分类、关键字查询

        // 分页参数
        int pageSize = 2; // 每页显示记录数
        int curPage = 2; // 当前页码
        int begin = pageSize * (curPage - 1); // 开始记录下标
        query.setStart(begin); // 起始下标
        query.setRows(pageSize); // 结束下标

        // 设置高亮参数
        query.setHighlight(true); // 开启高亮组件
        query.addHighlightField("product_name");// 高亮字段
        query.setHighlightSimplePre("<span color='red'>");//        前缀标记
        query.setHighlightSimplePost("</span>");// 后缀标记
        QueryResponse response = solrServer.query(query); // 请求查询
        SolrDocumentList docs = response.getResults();
        System.out.println("查询文档总数" + docs.getNumFound());
        for (SolrDocument doc : docs) {
            String id = (String) doc.getFieldValue("id");
            String product_name = (String) doc.getFieldValue("product_name");
            Float product_price = (Float) doc.getFieldValue("product_price");
            String product_picture = (String) doc.getFieldValue("product_picture");
            String product_catalog_name = (String) doc.getFieldValue("product_catalog_name");

            System.out.println("=============================");
            System.out.println("id=" + id);
            System.out.println("product_name=" + product_name);
            System.out.println("product_price=" + product_price);
            System.out.println("product_picture=" + product_picture);
            System.out.println("product_catalog_name=" + product_catalog_name);

            // 高亮信息
            if (response.getHighlighting() != null) {
                if (response.getHighlighting().get(id) != null) {
                    Map<String, List<String>> map = response.getHighlighting().get(id);// 取出高亮片段
                    if (map.get("product_name") != null) {
                        for (String s : map.get("product_name")) {
                            System.out.println(s);
                        }
                    }
                }
            }
        }
        return docs;
    }



}