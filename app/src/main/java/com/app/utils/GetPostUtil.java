package com.app.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by 林逸磊 on 2017/9/26.
 */

public class GetPostUtil {
    public static String sendGet1111(String url, String params) {
        String result = "";

        String urlName = url + "?" + params;
        try {
            URL realUrl = new URL(urlName);
            HttpURLConnection conn =(HttpURLConnection) realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1)");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();

            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                result = line+"\n";
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * @param uploadUrl      上传路径参数
     * @param uploadFilePath 文件路径
     * @category 上传文件至Server的方法
     * @author ylbf_dev
     */
    public static String uploadFile(String uploadUrl, String uploadFilePath, String id, String avatar) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        String result = "";
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(end + twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"id\"" + end + end);
            dos.writeBytes(id + end);
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"avatar\"" + end + end);
            dos.writeBytes(avatar + end);
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"pic\"; filename=\""
                    + uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1) + "\"" + end);
            dos.writeBytes("Content-Type: application/octet-stream" + end);
            dos.writeBytes("Content-Transfer-Encoding: binary" + end);
            dos.writeBytes(end);
            // 文件通过输入流读到Java代码中-++++++++++++++++++++++++++++++`````````````````````````
            FileInputStream fis = new FileInputStream(uploadFilePath);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            fis.close();
            System.out.println("file send to server............");
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();
            // 读取服务器返回结果
            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            result = br.readLine();
            dos.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param uploadUrl      上传路径参数
     * @param uploadFilePath 文件路径
     * @category 上传文件至Server的方法
     * @author ylbf_dev
     */
    public static String uploadFiletiezi(String uploadUrl, List<String> uploadFilePath, String id, String content) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        String result = "";
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());


            dos.writeBytes(end + twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"id\"" + end + end);
            dos.writeBytes(id);
            dos.writeBytes(end + twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"content\"" + end + end);
            dos.write((content+ end).getBytes());
            for (int i = 0; i < uploadFilePath.size(); i++) {
                String uploadFile = uploadFilePath.get(i);
//                String filename = uploadFile.substring(uploadFile.lastIndexOf("//") + 1);
                dos.writeBytes(twoHyphens + boundary + end);
                dos.writeBytes("Content-Disposition: form-data; name=\"file[]\"; filename=\""
                        + uploadFile.substring(uploadFilePath.lastIndexOf("/") + 1) + "\"" + end);
                dos.writeBytes("Content-Type: application/octet-stream" + end);
                dos.writeBytes("Content-Transfer-Encoding: binary" + end);
                dos.writeBytes(end);
                FileInputStream fStream = new FileInputStream(uploadFile);
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int length = -1;
                while ((length = fStream.read(buffer)) != -1) {
                    dos.write(buffer, 0, length);
                }
                dos.writeBytes(end);
              /* close streams */
                fStream.close();
            }
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();
            Log.w("1111.....", "帖子上传中。。。。");
            // 读取服务器返回结果
            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            result = br.readLine();
            dos.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String sendPost(String url, String params) {
        String result = "";
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("POST");
           conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            DataOutputStream dos=new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(params);
            dos.flush();
            dos.close();

            int resultCode=conn.getResponseCode();
            if(HttpURLConnection.HTTP_OK==resultCode){
                StringBuffer sb=new StringBuffer();
                String readLine=new String();
                BufferedReader responseReader=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                while((readLine=responseReader.readLine())!=null){
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                result=sb.toString();
                System.out.println(sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
            return result;
        }

}
