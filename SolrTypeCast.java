package com.qhit.utils.solr;

import com.qhit.goodsInfo.pojo.GoodsInfo;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 17194 on 2019/5/15.
 */
public class SolrTypeCast {

    public static void main(String[] args) {
        Student student = new Student();
        student.setSname("张三");
        student.setAge("20");
        SolrDocument solrDocument = toSolrDocument(student);
        System.out.println(solrDocument.toString());
    }

    /**
     * 请Bean转换为SolrDocument
     * @param object
     * @return
     */
    public static SolrInputDocument toSolrInputDocument(Object object){
        SolrInputDocument solrInputDocument = new SolrInputDocument();
        try {
            Class clazz = (Class) object.getClass();
            Field[] fields = clazz.getFields();
            for(Field field:fields){
                Object value = field.get(object);
                if (value!=null && value!=""){
                    field.setAccessible(true);
                    String name = field.getName();
                    solrInputDocument.addField(name,value);
                }
            }
            return solrInputDocument;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 请Bean转换为SolrDocument
     * @param object
     * @return
     */
    public static SolrDocument toSolrDocument(Object object){
        SolrDocument solrDocument = new SolrDocument();
        try {
            Class clazz = (Class) object.getClass();

            Field[] fields = clazz.getFields();
            for(Field field:fields){
                Object value = field.get(object);
                if (value!=null && value!=""){
                    field.setAccessible(true);
                    String name = field.getName();
                    solrDocument.addField(name,value);
                }
            }
            return solrDocument;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 将SolrDocument转换成Bean
     * @param record
     * @param clazz
     * @return
     */
    public static Object toBean(SolrDocument record, Class clazz){
        Object obj = null;
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
        Field[] fields = clazz.getDeclaredFields();
        for(Field field:fields){
            Object value = record.get(field.getName());
            try {
                BeanUtils.setProperty(obj, field.getName(), value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    /**
     * 将SolrDocumentList转换成BeanList
     * @param records
     * @param clazz
     * @return
     */
    public static Object toBeanList(SolrDocumentList records, Class clazz){
        List list = new ArrayList();
        for(SolrDocument record : records){
            list.add(toBean(record,clazz));
        }
        return list;
    }

}
