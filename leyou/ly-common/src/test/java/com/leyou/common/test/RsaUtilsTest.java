package com.leyou.common.test;
import com.leyou.common.auth.utils.RsaUtils;
import org.junit.Test;

public class RsaUtilsTest {

    private final static String publicKeyFileName = "E:\\rsa\\id_rsa.pub";
    private final static String privateKeyFileName = "E:\\rsa\\id_rsa";


    /**
     * 测试生成公钥和私钥
     */
    @Test
    public void generateKey() throws Exception {

        RsaUtils.generateKey(publicKeyFileName,privateKeyFileName,"123456",0);

    }
}