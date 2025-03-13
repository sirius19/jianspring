package com.jianspring.start.utils.object;
 
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
 
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
 
/**
 * @Author:  InfoInsights
 * @date 2022/8/8 9:17
 */
 
public class RsaUtils {
 
    /**
     * 类型
     */
    public static final String ENCRYPT_TYPE = "RSA";
 
    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";
 
    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";
 
    public static void main(String[] args) {
 
 
        Map<String, String> stringStringMap = RsaUtils.generateKeyPair();
 
        String RSAPublicKey = stringStringMap.get(PUBLIC_KEY);
 
        String RSAPrivateKey = stringStringMap.get(PRIVATE_KEY);
 
        System.out.println(RSAPublicKey);
        System.out.println("**************************");
        System.out.println(RSAPrivateKey);
        System.out.println("##########################");
        String content = "湾湾是中国的";
 
        String encrypt = RsaUtils.encrypt(content, RSAPublicKey);
 
        String decrypt = RsaUtils.decrypt(encrypt, RSAPrivateKey);
        System.out.println(decrypt);
 
    }
 
    public static Map<String, String> generateKeyPair() {
        try {
            KeyPair pair = SecureUtil.generateKeyPair(ENCRYPT_TYPE);
            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();
            // 获取 公钥和私钥 的 编码格式（通过该 编码格式 可以反过来 生成公钥和私钥对象）
            byte[] pubEncBytes = publicKey.getEncoded();
            byte[] priEncBytes = privateKey.getEncoded();
 
            // 把 公钥和私钥 的 编码格式 转换为 Base64文本 方便保存
            String pubEncBase64 = Base64.getEncoder().encodeToString(pubEncBytes);
            String priEncBase64 = Base64.getEncoder().encodeToString(priEncBytes);
 
            Map<String, String> map = new HashMap<String, String>(2);
            map.put(PUBLIC_KEY, pubEncBase64);
            map.put(PRIVATE_KEY, priEncBase64);
 
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
 
    /**
     * 公钥加密
     *
     * @param content   要加密的内容
     * @param publicKey 公钥
     */
    public static String encrypt(String content, String publicKey) {
        try {
            RSA rsa = new RSA(null, publicKey);
            return rsa.encryptBase64(content, KeyType.PublicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
 
    /**
     * 私钥解密
     *
     * @param content    要解密的内容
     * @param privateKey 私钥
     */
    public static String decrypt(String content, String privateKey) {
        try {
            RSA rsa = new RSA(privateKey, null);
            return rsa.decryptStr(content, KeyType.PrivateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
 