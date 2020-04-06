package com.leyou.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.config.OSSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class UploadService {

    @Autowired
    private OSS client;

    @Autowired
    private OSSProperties ossProperties;

    // 图片上传的路径
    private static final String IMAGE_PATH = "E:\\Develop\\nginx-1.16.0\\html\\brand-img\\";
    // 图片的访问路径
    private static final String IMAGE_URL = "http://image.leyou.com/brand-img/";

    // 允许上传的图片类型
    private static final List<String> ALLOW_UPLOAD_CONTENT_TYPE = Arrays.asList("image/png","image/jpg","image/jpeg");

    public String upload(MultipartFile file) {

        //  获取文件的类型： content-type
        String contentType = file.getContentType();

        //是否在规定的范围内
        if (!ALLOW_UPLOAD_CONTENT_TYPE.contains(contentType)){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        //就算改了文件后缀，也可以判断是否为图片ImageIO.read
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage==null){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        //保存文件夹
        File imagePath = new File(IMAGE_PATH);
        //图片名称
        String imageName = UUID.randomUUID()+file.getOriginalFilename();

        try {
            file.transferTo(new File(imagePath,imageName));
        } catch (IOException e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }

        String imgUrl = IMAGE_URL + imageName;

        return imgUrl;
    }


    /**
     * 获取阿里云签名
     * @return
     */

    public Map<String, String> signature() {

        try {
            long expireTime = ossProperties.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, ossProperties.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, ossProperties.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessId", ossProperties.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", ossProperties.getDir());
            respMap.put("host", ossProperties.getHost());
            respMap.put("expire", String.valueOf(expireEndTime));
            log.info("【上传微服务】上传到阿里云的签名成功");
            return respMap;

        } catch (Exception e) {
            log.info("【上传微服务】上传到阿里云的签名失败" + e.getMessage());
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }

    }
}
