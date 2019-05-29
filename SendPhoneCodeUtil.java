package com.qhit.utils.sendcode;

import java.security.MessageDigest;
import java.util.*;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


/**
 * 通过网易云信发送验证码
 * Created by 17194 on 2019/2/27.
 */
public class SendPhoneCodeUtil {
    //发送验证码的请求路径URL
    private static final String
            SERVER_URL="https://api.netease.im/sms/sendcode.action";
    //网易云信分配的账号，请替换你在管理后台应用下申请的Appkey
    private static final String APP_KEY="******";
    //网易云信分配的密钥，请替换你在管理后台应用下申请的appSecret
    private static final String APP_SECRET="******";
    //随机数
    private static final String NONCE="123456";
    //短信模板ID 从官网短信模板获取
    private static final String TEMPLATEID="14799057";
    //手机号 收件人的手机号
//    private static final String MOBILE="******";
    //验证码长度，范围4～10，默认为4
    private static final String CODELEN="6";


    /**
     * 测试
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> send = send("******");
        for (String s:send.keySet()){
            System.out.println(send.get(s));
        }

    }

    /**
     *
     * @param MOBILE 接收短信人
     * @throws Exception
     */
    public static Map<String, String> send(String MOBILE)throws Exception{
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(SERVER_URL);
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        /*
         * 计算CheckSum的java代码
         */
        String checkSum = getCheckSum(APP_SECRET, NONCE, curTime);

        // 设置请求的header
        httpPost.addHeader("AppKey", APP_KEY);
        httpPost.addHeader("Nonce", NONCE);
        httpPost.addHeader("CurTime", curTime);
        httpPost.addHeader("CheckSum", checkSum);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // 设置请求的的参数，requestBody参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        /*
         * 1.如果是模板短信，请注意参数mobile是有s的，详细参数配置请参考“发送模板短信文档”
         * 2.参数格式是jsonArray的格式，例如 "['13888888888','13666666666']"
         * 3.params是根据你模板里面有几个参数，那里面的参数也是jsonArray格式
         */
        nvps.add(new BasicNameValuePair("templateid", TEMPLATEID));
        nvps.add(new BasicNameValuePair("mobile", MOBILE));
        nvps.add(new BasicNameValuePair("codeLen", CODELEN));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));

        // 执行请求
        HttpResponse response = httpClient.execute(httpPost);
        /*
         * 1.打印执行结果，打印结果一般会200、315、403、404、413、414、500
         * 2.具体的code有问题的可以参考官网的Code状态表
         */
        String string = EntityUtils.toString(response.getEntity(), "utf-8");
        //String string = "{\"code\":200,\"msg\":\"1\",\"obj\":\"570453\"}";//返回格式(json)
        //把返回对象转为map
        Map<String,Object> map = new Gson().fromJson(string, Map.class);
        for(String s:map.keySet()){
            //把code转为int，在转为string
            if("code".equals(s)){
                double o = (double)map.get(s);
                int o2 = (int) o;
                map.put(s,""+o2);
            }
        }
        Map<String,String> returnMap = new HashMap();
        String code = (String) map.get("code");
        if("200".equals(code)){
            //发送成功
            returnMap.put("message","发送成功!");
            returnMap.put("yanzhengma",""+map.get("obj"));
            returnMap.put("state",""+1);
        }else {
            //发送失败
            map.put("yanzhengma",map.get(""));
            returnMap.put("state",""+0);
            if("201".equals(code)){
                //客户端版本不对，需升级sdk
                returnMap.put("message","发送失败，客户端版本不对，需升级sdk");
            }else if("408".equals(code)){
                //客户端请求超时
                returnMap.put("message","发送失败，客户端请求超时");
            }else if("315".equals(code)){
                //IP限制
                returnMap.put("message","发送失败，IP限制");
            }else if("414".equals(code)){
                //参数错误
                returnMap.put("message","发送失败，参数错误");
            }else if("417".equals(code)){
                //重复操作
                returnMap.put("message","发送失败，重复操作");
            }else if("419".equals(code)){
                //数量超过上限
                returnMap.put("message","发送失败，数量超过上限");
            }else {
                returnMap.put("message","发送失败!");
            }
        }
        return returnMap;


    }

    // 计算并获取CheckSum
    public static String getCheckSum(String appSecret, String nonce, String curTime) {
        return encode("sha1", appSecret + nonce + curTime);
    }

    // 计算并获取md5值
    public static String getMD5(String requestBody) {
        return encode("md5", requestBody);
    }

    private static String encode(String algorithm, String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest messageDigest
                    = MessageDigest.getInstance(algorithm);
            messageDigest.update(value.getBytes());
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
}
