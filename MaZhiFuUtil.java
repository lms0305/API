package com.dhcc.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 码支付
 * Created by GeneratorCode on 2019/03/25
 */
public class MaZhiFuUtil{

    /**
     * 接收参数 创建订单
     * @param request
     * @param response
     * @throws IOException
     */
    public void creatOrders( HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = "zWfIcQH4tkGg96VExDYY9pgULpxXn4HM"; //记得更改 http://codepay.fateqq.com 后台可设置
        String codepay_id ="216813" ;//记得更改 http://codepay.fateqq.com 后台可获得
        Date date=new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String format = df.format(date);
        String price=request.getParameter("price"); //表单提交的价格
        String type=request.getParameter("type"); //支付类型  1：支付宝 2：QQ钱包 3：微信
        String pay_id=request.getParameter("pay_id"); //支付人的唯一标识
        String param=request.getParameter("param"); //自定义一些参数 支付后返回

        String notify_url="http://172.25.20.131/ordersInfo/execute";//通知地址
        String return_url="http://172.25.20.131/ordersInfo/execute";//支付后同步跳转地址
        //参数有中文则需要URL编码       把提交的价格+付款的类型+返回后需要的信息+支付后的跳转路径  转发到官网上
        String url="http://codepay.fateqq.com:52888/creat_order?id="+codepay_id+
                "&pay_id=0&price=0.5&type=1&token="+token+
                "&param="+param+"&notify_url="+notify_url+"&return_url="+return_url;
        response.sendRedirect(url);
    }

    /**
     * 验证通知 处理自己的业务支付后跳转的地址
     * @param request
     * @param response
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public void execute( HttpServletRequest request, HttpServletResponse response) throws NoSuchAlgorithmException, IOException {
        String key = "bFrU2uhat2uEwTIyiA3Hv3onp399AY"; //记得更改 http://codepay.fateqq.com 后台可设置
        Map<String,String> params = new HashMap<String,String>(); //申明hashMap变量储存接收到的参数名用于排序
        Map requestParams = request.getParameterMap(); //获取请求的全部参数
        String valueStr = ""; //申明字符变量 保存接收到的变量
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            valueStr = values[0];
            //乱码解决，这段代码在出现乱码时使用。如果sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);//增加到params保存
        }
        List<String> keys = new ArrayList<String>(params.keySet()); //转为数组
        Collections.sort(keys); //重新排序
        String prestr = "";
        String sign= params.get("sign"); //获取接收到的sign 参数
        for (int i = 0; i < keys.size(); i++) { //遍历拼接url 拼接成a=1&b=2 进行MD5签名
            String key_name = keys.get(i);
            String value = params.get(key_name);
            if(value== null || value.equals("") ||key_name.equals("sign")){ //跳过这些 不签名
                continue;
            }
            if (prestr.equals("")){
                prestr =  key_name + "=" + value;
            }else{
                prestr =  prestr +"&" + key_name + "=" + value;
            }
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update((prestr+key).getBytes());
        String  mySign = new BigInteger(1, md.digest()).toString(16).toLowerCase();
        if(mySign.length()!=32)mySign="0"+mySign;
        if(mySign.equals(sign)){
//            编码要匹配 编码不一致中文会导致加密结果不一致
//            参数合法处理业务
            String pay_no = request.getParameter("pay_no");//流水号
            String pay_id = request.getParameter("pay_id");//用户唯一标识
            String money = request.getParameter("money");//付款金额
            String price = request.getParameter("price");//提交的金额
            //成功后跳转的地址
            response.sendRedirect("http://172.25.20.131:9400/success.html");
        }else{
            //参数不合法
            System.out.print("fail");
            //失败后跳转的地址
            response.sendRedirect("http://172.25.20.131:9400/errenr.html");
        }
    }




}