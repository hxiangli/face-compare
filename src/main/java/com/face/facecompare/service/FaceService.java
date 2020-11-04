package com.face.facecompare.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.face.facecompare.controller.ResponseResult;
import com.face.facecompare.jna.IWeiXinBrainApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FaceService {

    /**
     * 在程序启动时加载C语言dll文件,交给spring容器管理
     */
    private static final long serialVersionUID = -3129130646476134549L;

    public static Map<String, Map<String, Object>> apiMap = new HashMap<String, Map<String, Object>>();
   static {

       HashMap<String, Map<String, String>>dicMap = new HashMap<String, Map<String, String>>();
       Map<String, String> typeIdMap = new HashMap<String, String>();
       typeIdMap.put("1", "人脸检测");
       typeIdMap.put("2", "人脸特征提取");
       typeIdMap.put("3", "人脸性别识别");
       typeIdMap.put("4", "人脸年龄识别");
       dicMap.put("101", typeIdMap);

           //apiMap.clear();
           for(String typeId :dicMap.get("101").keySet()) {
               if(!apiMap.containsKey(typeId)){
                   FaceService api = new FaceService();
                   int nInstanceID = api.WXAiGetInstance(Integer.parseInt(typeId), 0);
                   System.out.println(nInstanceID+"成功注册");
                   if(nInstanceID <= 0){
                       System.out.println("初始化实例错误：" + api.WXAiGetLastError(nInstanceID));
                       continue;
                   }
                   Map<String, Object> oneMap = new HashMap<String, Object>();
                   oneMap.put("api", api);
                   oneMap.put("id", nInstanceID);
                   apiMap.put(typeId, oneMap);

           }
       }
   }

    /**
     * 人脸比对，返回相似度
     * @param im1base64
     * @param img2
     * @return
     */
   public ResponseResult faceCompare(String im1base64, String img2){
       ResponseResult responseResult = new ResponseResult();
       //人脸检测
       JSONObject jsoninput = new JSONObject();
       jsoninput.put("typeId", 1);
       jsoninput.put("format", 1);
       jsoninput.put("input", im1base64);
       jsoninput.put("minFaceSize", 50);
       Map<String, Object> oneMap = FaceService.apiMap.get("1");
       String detectJson =  this.WXAiDetect((Integer) oneMap.get("id"), jsoninput.toJSONString());
       if(StringUtils.isEmpty(detectJson)){
           log.info("==人脸检测结果为空 im1base64 {}", im1base64);
           responseResult.setMessage("人脸检测结果为空");
           responseResult.setSuccess(false);
           responseResult.setCode("-1");//未检测到人脸
           return responseResult;
       }


       // 人脸特征提取
       JSONObject json = new JSONObject();
       json.put("typeId", 2);
       JSONArray jsonArray = new JSONArray();
       jsonArray.add(im1base64);
       jsonArray.add(img2);
       json.put("input", jsonArray);
       json.put("minFaceSize", 50);


       //jna 请求
       Map<String, Object> oneMap2 = FaceService.apiMap.get("2");
       String facetzStr = this.WXAiDetect((Integer) oneMap2.get("id"), json.toJSONString());

       //解析结果
       JSONObject facetzjson = JSONObject.parseObject(facetzStr);
       JSONArray outjsonArray = facetzjson.getJSONArray("output");
       String img1tzflat = outjsonArray.getString(0);
       String img2tzflat = outjsonArray.getString(1);

       if(StringUtils.isEmpty(img1tzflat) || StringUtils.isEmpty(img2tzflat)){
           log.info("==人脸特征提取结果为空 im1base64 {}", im1base64);
           responseResult.setMessage("人脸特征提取结果为空");
           responseResult.setSuccess(false);
           responseResult.setCode("-1");//未检测到人脸
           return responseResult;
       }

       //转成float 数组
       //第一张图片
       float[] pFeature1s = new float[128];
       String[] img1tzflatArr = img1tzflat.split(",");
       for (int i=0;i<img1tzflatArr.length; i++) {
           pFeature1s[i] = Float.parseFloat(img1tzflatArr[i]);
       }

       //第二张图片
       float[] pFeature2s = new float[128];
       String[] img2tzflatArr = img2tzflat.split(",");
       for (int i=0;i<img2tzflatArr.length; i++) {
           pFeature2s[i] = Float.parseFloat(img2tzflatArr[i]);
       }

       //人脸识别相似度对比
       float result = this.WXAiCalSimilar(pFeature1s, pFeature2s, facetzjson.getInteger("featureDim"));

       log.info("相似度为=======================："+result);
       responseResult.setSuccess(true);
       responseResult.setContent(result);
       return responseResult;

   }

    public static void main(String[] args) {
        FaceService faceService =  new FaceService();
        //获取比对图片的base64位
        String img1 = "C:\\Users\\ASUS\\Desktop\\test.png";
        String img2 = "C:\\Users\\ASUS\\Desktop\\zp.jpg";


        String im1base64 = faceService.getimgBase64(img1);
        String im2base64 = faceService.getimgBase64(img2);

        //人脸检测
        JSONObject jsoninput = new JSONObject();
        jsoninput.put("typeId", 1);
        jsoninput.put("format", 1);
        jsoninput.put("input", im1base64);
        jsoninput.put("minFaceSize", 50);
        Map<String, Object> oneMap = FaceService.apiMap.get("1");
        String detectJson =  faceService.WXAiDetect((Integer) oneMap.get("id"), jsoninput.toJSONString());
        if(StringUtils.isEmpty(detectJson)){
            System.out.println("人脸检测结果为空");
            return;
        }


        // 人脸特征提取
        JSONObject json = new JSONObject();
        json.put("typeId", 2);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(im1base64);
        jsonArray.add(im2base64);
        json.put("input", jsonArray);
        json.put("minFaceSize", 50);


        //jna 请求
        Map<String, Object> oneMap2 = FaceService.apiMap.get("2");
        String facetzStr = faceService.WXAiDetect((Integer) oneMap2.get("id"), json.toJSONString());

        //解析结果
        JSONObject facetzjson = JSONObject.parseObject(facetzStr);
        JSONArray outjsonArray = facetzjson.getJSONArray("output");
        String img1tzflat = outjsonArray.getString(0);
        String img2tzflat = outjsonArray.getString(1);

        if(StringUtils.isEmpty(img1tzflat) || StringUtils.isEmpty(img2tzflat)){
            System.out.println("==图片未检测到人像");
            return;
        }

        //转成float 数组
        //第一张图片
        float[] pFeature1s = new float[128];
        String[] img1tzflatArr = img1tzflat.split(",");
        for (int i=0;i<img1tzflatArr.length; i++) {
            pFeature1s[i] = Float.parseFloat(img1tzflatArr[i]);
        }

        //第二张图片
        float[] pFeature2s = new float[128];
        String[] img2tzflatArr = img2tzflat.split(",");
        for (int i=0;i<img2tzflatArr.length; i++) {
            pFeature2s[i] = Float.parseFloat(img2tzflatArr[i]);
        }

        //人脸识别相似度对比
        float result = faceService.WXAiCalSimilar(pFeature1s, pFeature2s, facetzjson.getInteger("featureDim"));

        System.out.println("相似度为=======================："+result);



    }

    /**
     * 获取图片base64
     * @return
     */
    public String getimgBase64(String path){
        byte[] imgData = null;
        InputStream in = null;

        try {
            in = new FileInputStream(path);
            imgData = new byte[in.available()];
            in.read(imgData);
        }catch (IOException e1){
        }finally {
            try {
                in.close();
            }catch (IOException e2){
            }
        }

        return Base64Utils.encodeToString(imgData);
    }


    public int WXAiGetInstance(int nTypeId, int nDeviceID) {
        return IWeiXinBrainApi.INSTANCE.WXAiGetInstance(nTypeId, nDeviceID);
    }

    public String WXAiDetect(int nInstanceID, String strParam) {
        return IWeiXinBrainApi.INSTANCE.WXAiDetect(nInstanceID, strParam);
    }

    public int WXAiGetLastError(int nInstanceID) {
        return IWeiXinBrainApi.INSTANCE.WXAiGetLastError(nInstanceID);
    }

    public float WXAiCalSimilar(float[] pFeature1, float[] pFeature2, int nFeatureLen) {
        return IWeiXinBrainApi.INSTANCE.WXAiCalSimilar(pFeature1, pFeature2,nFeatureLen);
    }

}
