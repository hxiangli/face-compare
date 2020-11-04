package com.face.facecompare.controller;

import com.face.facecompare.service.FaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: hxl
 * @Date: 2020/11/3 10:27
 */
@Controller
@RequestMapping("/")
@Slf4j
public class FaceController {

    @Autowired
    private FaceService faceService;
    /**
     * 比对人脸图片相似度
     * @return
     */
    @RequestMapping("/faceCompare")
    @ResponseBody
    public ResponseResult addCaseRole(String img1,String img2) {
        ResponseResult responseResult = new ResponseResult();
      //  log.info("==进入人脸比对 img1 {} img2 {}", img1,img2);
        if(StringUtils.isEmpty(img1) || StringUtils.isEmpty(img2)){
            log.error("==图片参数不能为空");
            responseResult.setSuccess(false);
            responseResult.setMessage("图片数据不能为空");
            responseResult.setCode("-9");//入参不能为空
            return responseResult;
        }
        responseResult = faceService.faceCompare(img1, img2);
        return responseResult;
    }


    /**
     * 首页
     * @return
     */
    @RequestMapping("/")
    public String showHome() {
        return "index";
    }


    /**
     * 图片集合
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseResult list() {
        //获取当前目录img下的图片
       String imgPath =  System.getProperty("user.dir")+"\\faceImg";
       //读取目录下图片base64
        String[] chillist=new File(imgPath).list();
        List<String> imgs = new ArrayList<>();
        for (String chil : chillist) {
            if(chil.endsWith("jpg") || chil.endsWith("png")){

              String imgbase64 =   faceService.getimgBase64(imgPath+"\\"+chil);
                imgs.add(imgbase64);
            }
        }
        ResponseResult responseResult = new ResponseResult();
        responseResult.setContent(imgs);

        return responseResult;
    }



}
