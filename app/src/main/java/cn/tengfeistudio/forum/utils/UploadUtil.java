package cn.tengfeistudio.forum.utils;



import android.util.Log;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by xietufei on 2019/3/13.
 */

public class UploadUtil {
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10 * 1000;//超时时间
    private static final String CHARSET = "utf-8";//设置编码

    /**
     * android上传文件到服务器
     *
     * @param files       需要上传的文件
     * @param RequestURL  请求的url
     * @return 返回响应的内容
     */
    public static String uploadImage(String token,String title,String content,String theme,ArrayList<String> files, String RequestURL) {
        String result = "error";
        String BOUNDARY = UUID.randomUUID().toString();//边界标识 随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";//内容类型
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);//允许输入流
            conn.setDoOutput(true);//允许输出流
            conn.setUseCaches(false);//不允许使用缓存
            conn.setRequestMethod("POST");//请求方式
            conn.setRequestProperty("Charset", CHARSET);//设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            conn.connect();
            if (files != null && files.size()>0) {
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuffer sb1 = new StringBuffer();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINE_END);
                sb1.append("Content-Disposition: form-data; name=\"token\"; filename=\"" + token + "\"" + LINE_END);
                sb1.append("Content-Type: application/octet-stream; charset=UTF-8" + LINE_END);
                sb1.append(LINE_END);
                Log.i("------------", "--Header--"+sb1.toString());
                dos.write(sb1.toString().getBytes());
                dos.write(LINE_END.getBytes());

                StringBuffer sb2 = new StringBuffer();
                sb2.append(PREFIX);
                sb2.append(BOUNDARY);
                sb2.append(LINE_END);
                sb2.append("Content-Disposition: form-data; name=\"title\"; filename=\"" + title + "\"" + LINE_END);
                sb2.append("Content-Type: application/octet-stream; charset=UTF-8" + LINE_END);
                sb2.append(LINE_END);
                Log.i("------------", "--Header--"+sb2.toString());
                dos.write(sb2.toString().getBytes());
                dos.write(LINE_END.getBytes());

                StringBuffer sb3 = new StringBuffer();
                sb3.append(PREFIX);
                sb3.append(BOUNDARY);
                sb3.append(LINE_END);
                sb3.append("Content-Disposition: form-data; name=\"content\"; filename=\"" + content + "\"" + LINE_END);
                sb3.append("Content-Type: application/octet-stream; charset=UTF-8" + LINE_END);
                sb3.append(LINE_END);
                Log.i("------------", "--Header--"+sb1.toString());
                dos.write(sb3.toString().getBytes());
                dos.write(LINE_END.getBytes());

                StringBuffer sb4 = new StringBuffer();
                sb4.append(PREFIX);
                sb4.append(BOUNDARY);
                sb4.append(LINE_END);
                sb4.append("Content-Disposition: form-data; name=\"theme\"; filename=\"" + theme + "\"" + LINE_END);
                sb4.append("Content-Type: application/octet-stream; charset=UTF-8" + LINE_END);
                sb4.append(LINE_END);
                Log.i("------------", "--Header--"+sb4.toString());
                dos.write(sb4.toString().getBytes());
                dos.write(LINE_END.getBytes());

//                //设置token
//                dos=setParameter(dos,"token",token);
//                //设置title
//                dos=setParameter(dos,"title",title);
//                //设置content
//                dos=setParameter(dos,"content",content);
//                //设置theme
//                dos=setParameter(dos,"theme",theme);


                //设置文件
                for (int i = 0; i < files.size(); i++) {
                    File file=new File(files.get(i).toString());
                    Log.i("--------------------", "file"+i+"="+file.getName());
                    /**
                     * 当文件不为空，把文件包装并且上传
                     */
                    StringBuffer sb = new StringBuffer();
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINE_END);
                    /**
                     * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
                     * filename是文件的名字，包含后缀名的 比如:abc.png
                     */
                    sb.append("Content-Disposition: form-data; name=\"inputName\"; filename=\"" + file.getName() + "\"" + LINE_END);
                    sb.append("Content-Type: application/octet-stream; charset=UTF-8" + LINE_END);
                    sb.append(LINE_END);
                    Log.i("------------", "--Header--"+sb.toString());
                    dos.write(sb.toString().getBytes());

                    InputStream is = new FileInputStream(file);
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    while ((len = is.read(bytes)) != -1) {
                        dos.write(bytes, 0, len);
                    }
                    is.close();
                    dos.write(LINE_END.getBytes());

                }

                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();

                //当文件不为空，把文件包装并且上传
//                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
//                dos.writeBytes(PREFIX + BOUNDARY + LINE_END);
//                dos.writeBytes("Content-Disposition: form-data; " + "name=\"inputName\";filename=\"" + file.getName() + "\"" + LINE_END);
//                dos.writeBytes(LINE_END);
//
//                FileInputStream is = new FileInputStream(file);
//                byte[] bytes = new byte[1024];
//                int len = -1;
//                while ((len = is.read(bytes)) != -1) {
//                    dos.write(bytes, 0, len);
//                }
//                is.close();
//                dos.write(LINE_END.getBytes());
//
//                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
//                dos.write(end_data);
//                dos.flush();
                /*
          *  获取响应码  200=成功
         * 当响应成功，获取响应的流  
         */
                int res = conn.getResponseCode();
                if (res == 200) {
                    InputStream input = conn.getInputStream();
                    StringBuilder sbs = new StringBuilder();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sbs.append((char) ss);
                    }
                    result = sbs.toString();
                    Log.i(TAG, "result------------------>>" + result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static DataOutputStream setParameter(DataOutputStream dos,String tag,String parameter){
        try {
            String result = "error";
            String BOUNDARY = UUID.randomUUID().toString();//边界标识 随机生成
            String PREFIX = "--", LINE_END = "\r\n";
            String CONTENT_TYPE = "multipart/form-data";//内容类型
            /**
             * 当文件不为空，把文件包装并且上传
             */
            StringBuffer sb1 = new StringBuffer();
            sb1.append(PREFIX);
            sb1.append(BOUNDARY);
            sb1.append(LINE_END);
            /**
             * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
             * filename是文件的名字，包含后缀名的 比如:abc.png
             */
            sb1.append("Content-Disposition: form-data; name=\"" + tag + "\"; filename=\"" + parameter + "\"" + LINE_END);
            sb1.append("Content-Type: application/octet-stream; charset=UTF-8" + LINE_END);
            sb1.append(LINE_END);
            Log.i("------------", "--Header--"+sb1.toString());
            dos.write(sb1.toString().getBytes());
            dos.write(LINE_END.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dos;
    }

    /**
     * 判断文件类型
     * @param file
     * @return
     */
    private static String getMIMEType(File file) {
        String fileName = file.getName();
        if (fileName.endsWith("png") || fileName.endsWith("PNG")) {
            return "image/png";
        } else {
            return "image/jpg";
        }
    }


    public static URL picCOS(File cosFile) throws Exception {
        String SecretId="AKID1sjOt07V3Vk8LcscJl6E8oDOKAnF2Xaa";
        String SecretKey="wgFZufBMK3YAXhjZ5PaYLuEe43mLfhT1";
        COSCredentials cred = new BasicCOSCredentials(SecretId,SecretKey);
        // 2 设置bucket的区域, COS地域的简称请参照
        // https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-guangzhou"));
        // 3 生成cos客户端
        COSClient cosClient = new COSClient(cred, clientConfig);
        String bucketName = "tengfeistudio"+"-1252503273";
        String key = "images/"+new Date().getTime() + ".png";
        // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20 M 以下的文件使用该接口
        // 大文件上传请参照 API 文档高级 API 上传
        // 指定要上传到 COS 上的路径
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, cosFile);
        cosClient.putObject(putObjectRequest);
        cosClient.shutdown();
        Date expiration = new Date(new Date().getTime() + 5 * 60 * 10000);
        URL url = cosClient.generatePresignedUrl(bucketName, key, expiration);
        return url;
    }


    /**
     * 上传头像进行更改
     * @param token
     * @param files
     * @param RequestURL
     * @return
     */
    public static String uploadIcon(String token,ArrayList<String> files, String RequestURL) {
        String result = "error";
        String BOUNDARY = UUID.randomUUID().toString();//边界标识 随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";//内容类型
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);//允许输入流
            conn.setDoOutput(true);//允许输出流
            conn.setUseCaches(false);//不允许使用缓存
            conn.setRequestMethod("POST");//请求方式
            conn.setRequestProperty("Charset", CHARSET);//设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            conn.connect();
            if (files != null && files.size()>0) {
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuffer sb1 = new StringBuffer();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINE_END);
                sb1.append("Content-Disposition: form-data; name=\"token\"; filename=\"" + token + "\"" + LINE_END);
                sb1.append("Content-Type: application/octet-stream; charset=UTF-8" + LINE_END);
                sb1.append(LINE_END);
                Log.i("------------", "--Header--"+sb1.toString());
                dos.write(sb1.toString().getBytes());
                dos.write(LINE_END.getBytes());


                //设置文件
                for (int i = 0; i < files.size(); i++) {
                    File file=new File(files.get(i).toString());
                    Log.i("--------------------", "file"+i+"="+file.getName());
                    /**
                     * 当文件不为空，把文件包装并且上传
                     */
                    StringBuffer sb = new StringBuffer();
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINE_END);
                    /**
                     * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
                     * filename是文件的名字，包含后缀名的 比如:abc.png
                     */
                    sb.append("Content-Disposition: form-data; name=\"inputName\"; filename=\"" + file.getName() + "\"" + LINE_END);
                    sb.append("Content-Type: application/octet-stream; charset=UTF-8" + LINE_END);
                    sb.append(LINE_END);
                    Log.i("------------", "--Header--"+sb.toString());
                    dos.write(sb.toString().getBytes());

                    InputStream is = new FileInputStream(file);
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    while ((len = is.read(bytes)) != -1) {
                        dos.write(bytes, 0, len);
                    }
                    is.close();
                    dos.write(LINE_END.getBytes());

                }

                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();

                /*
          *  获取响应码  200=成功
         * 当响应成功，获取响应的流  
         */
                int res = conn.getResponseCode();
                if (res == 200) {
                    InputStream input = conn.getInputStream();
                    StringBuilder sbs = new StringBuilder();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sbs.append((char) ss);
                    }
                    result = sbs.toString();
                    Log.i(TAG, "result------------------>>" + result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
