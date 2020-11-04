package com.face.facecompare.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface IWeiXinBrainApi extends Library {
 //F:\wenxinface\Dll\WeiXinBrainApi    Dll\WeiXinBrainApi

    IWeiXinBrainApi INSTANCE = (IWeiXinBrainApi) Native.loadLibrary(DDLpath.getDDLpath(), IWeiXinBrainApi.class);
    /**
     * 检测接口
     nTypeId - 检测类型ID
     nDeviceID - 显卡设备ID索引号，从0开始，-1表示使用cpu检测。
     * @return 成功返回大于等于1，失败返回小于0，见错误码。
     *@n History:    1.Date:2019/03/22
    2.Author:Leilh
    3.Modification:
     */
    int WXAiGetInstance(int nTypeId, int nDeviceID);

    /**
     * 检测接口
     nInstanceID - 实例句柄
     strParam - 输入参数json格式字符串
     * @return 输出结果json格式字符串
     *@n History:    1.Date:2019/03/22
    2.Author:Leilh
    3.Modification:
     */
    String WXAiDetect(int nInstanceID, String strParam);

    /**
     * 释放实例
     nInstanceID - 实例句柄
     * @return 成功返回0，失败返回错误码。
     *@n History:    1.Date:2019/03/22
    2.Author:Leilh
    3.Modification:
     */
    int WXAiReleaseInstance(int nInstanceID);

    /**
     * 计算两种人脸图片特征的相似度
     pFeature1 - 人脸图片特征值1
     pFeature2 - 人脸图片特征值2
     nFeatureLen - 特征值维度
     * @return 两张人脸图片特征的相似度，取值范围0~1.0，值越大相似度越高。
     *@n History:    1.Date:2019/03/22
    2.Author:Leilh
    3.Modification:
     */
    float WXAiCalSimilar(float[] pFeature1, float[] pFeature2, int nFeatureLen);

    /**
     * 获取最后的错误码
     * @return 最后的错误码。
     *@n History:    1.Date:2019/03/22
    2.Author:Leilh
    3.Modification:
     */
    int WXAiGetLastError(int nInstanceID);

}


class  DDLpath{

    public  static  String  getDDLpath(){

        String ddlpath =   System.getProperty("user.dir")+"\\DLL\\WeiXinBrainApi.dll";
        System.out.println("==路径为"+ddlpath);
        return ddlpath;

    }
}





