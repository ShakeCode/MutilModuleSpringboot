package com.search.service.security;

import com.search.service.exception.ServiceException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * 1、DSA加密
 * <p>
 * 2、RSA加密 -- 非对称加密
 * <p>
 * 3、DES加密
 * 4、AES加密   -- 对称加密.Mysql也有类似函数
 * 5、MD5算法
 * 6、Base64加密算法
 * 7、异或加密算法
 * <p>
 * AES、DES为对称加密，RSA、DSA为非对称加密(更加安全)
 * <p>
 * Provider —— http://jszx-jxpt.cuit.edu.cn/JavaAPI/java/security/Provider.html
 * Security —— http://jszx-jxpt.cuit.edu.cn/JavaAPI/java/security/Security.html
 * JAC —— http://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html
 * <p>
 * 我们做Java开发，或是Android开发，都会先在电脑上安装JDK(Java Development Kit) 并配置环境变量，JDK也就是 Java 语言的软件开发工具包，JDK中包含有JRE（Java Runtime Environment，即：Java运行环境），JRE中包括Java虚拟机（Java Virtual Machine）、Java核心类库和支持文件，而我们今天要说的主角就在Java的核心类库中。在Java的核心类库中有一个JCE（Java Cryptography Extension），JCE是一组包，它们提供用于加密、密钥生成和协商以及 Message Authentication Code（MAC）算法的框架和实现，所以这个是实现加密解密的重要类库。
 * <p>
 * 在我们安装的JRE目录下有这样一个文件夹：%JAVE_HOME%\jre\lib\security（%JAVE_HOME%是自己电脑的Java路径，一版默认是：C:\Program Files\Java，具体看自己当时安装JDK和JRE时选择的路径是什么），其中包含有两个.jar文件：“local_policy.jar”和“US_export_policy.jar”，也就是我们平时说的jar包，再通俗一点说就是Java中包含的类库（Sun公司的程序大牛封装的类库，供使用Java开发的程序员使用），这两个jar包就是我们JCE中的核心类库了。JRE中自带的“local_policy.jar”和“US_export_policy.jar”是支持128位密钥的加密算法，而当我们要使用256位密钥算法的时候，已经超出它的范围，无法支持，所以才会报：“java.security.InvalidKeyException: Illegal key size or default parameters”的异常
 * <p>
 * Java是Sun开发的一种编程语言，2009年oracle宣布收购Sun公司，从此两家就是一家了），所以在oracle官网给我们提供有Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files X（即： Java加密扩展（JCE）无限强度权限政策文件），也就是所谓的JCE的无敌加强版，后面的“X”代表的是对应的JDK版本。该文件中只包含了 “local_policy.jar”和“US_export_policy.jar”这两个jar包，我们只需要拿这两个jar包替换掉自己JRE中的对应jar包就行了
 * <p>
 * JDK8：
 * <p>
 * 其对应的JCE下载地址为：http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
 * <p>
 * 下载完后，解压，将其中的“local_policy.jar”和“US_export_policy.jar”两个文件替换掉自己%JAVE_HOME%\jre\lib\security文件夹下对应的原文件（%JAVE_HOME%是自己电脑的Java路径）。
 * <p>
 * oracel 官网：https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Cipher
 * <p>
 * <p>
 * 对称加解密算法AES +加解密模式CBC+ 填充模式PKCS5Padding + 128位加解密
 * <p>
 * 1.对称加密:  加密者和解密者用的是同一串秘钥
 * <p>
 * <p>
 * 2.AES有四种加解密模式：CBC、EBC、CFB、OFB 。CBC模式下，加密时，是对明文进行分组加密的，每组大小一致，到  最后 一组时，可能长度不够，这个时候就需要填充到一样长度，就有了下面的填充模式。
 * <p>
 * 3.有两种填充模式：PKCS5Padding、PKCS7Padding 。这两种填充模式填充规则一样，区别在于，分组时，每组约定大小不一样。PKCS5Padding要求块（block）的大小是8位，PKCS7Padding则没有明确，范围在1-255之间
 * <p>
 * <p>
 * 4.秘钥的长度，128位即128bit，AES的秘钥长度一般有：128bit、192bit、256bit。java 中，秘钥里的一个字符是一个字节（1byte），1byte=8bit。一个128位的秘钥，即由16个字符组成，如："aigov1266aba186k"
 * <p>
 * https://www.javainterviewpoint.com/aes-encryption-and-decryption/
 * <p>
 * https://www.baeldung.com/java-aes-encryption-decryption
 * <p>
 * 核心类：
 * SecretKeySpec 密钥,安全最高级别应该随机产生,由加密数据和秘钥随机产生
 * Cipher cipher 解密类,
 * IvParameterSpec 向量类，"AES/CBC/PKCS5Padding"初始化Cipher需要向量IV,"AES/ECB/PKCS5Padding"-默认的加密算法,不需要初始向量（IV）,所以容易被破解
 * The type Cipher util.
 */
@Service
public class CipherService {
    private final Logger LOGGER = LoggerFactory.getLogger(CipherService.class);

/*    private  int iterations = 65536;
    // 密钥长度128不需要特殊处理
    // 密钥长度是256需要特殊处理, 替换JCE jar包(local_policy.jar和US_export_policy.jar)
    private  int keySize = 128;
    private  byte[] ivBytes;
    private  SecretKey secretKey;*/

    private final Environment env;

    /*
     * 加密用的Key 可以用26个字母和数字组成，最好不要用保留字符，虽然不会错，至于怎么裁决，个人看情况而定
     * 此处使用AES-128-CBC加密模式，key需要为16位。
     */
    private final String secKey = "1234567890abcdef";

    /**
     * Instantiates a new Cipher service.
     * @param env the env
     */
    public CipherService(Environment env) {
        this.env = env;
    }

/*    public String encrypt(String plaintext) throws Exception {
        SecretKey secretKey = getSecretKey(plaintext);
        SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), getEnvProperty("Cipher.secret.key.sepc.algorithm"));
        Cipher cipher = Cipher.getInstance(getEnvProperty("Cipher.intance.algorithm"));
        cipher.init(Cipher.ENCRYPT_MODE, secretSpec);
        byte[] encryptedTextBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(encryptedTextBytes);
    }*/

    /*    public String decrypt(String encryptedText) throws Exception {
        byte[] encryptedTextBytes = Base64.decodeBase64(encryptedText);
        SecretKey secretKey = getSecretKey(encryptedText);
        SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), getEnvProperty("Cipher.secret.key.sepc.algorithm"));
        Cipher cipher = Cipher.getInstance(getEnvProperty("Cipher.intance.algorithm"));
        AlgorithmParameters params = cipher.getParameters();
        byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();
        cipher.init(Cipher.DECRYPT_MODE, secretSpec, new IvParameterSpec(ivBytes));
        byte[] decryptedTextBytes;
        try {
            decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("decrypt text error", e);
            throw new ServiceException("解密失败");
        }
        return new String(decryptedTextBytes, StandardCharsets.UTF_8);
    }*/

    // 解密
    public String decrypt(String plainText, String secKey) throws Exception {
        // 判断Key是否正确
        if (checkSecKey(secKey)) {
            throw new ServiceException("error sec key");
        }
        byte[] secKeyBytes = secKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secKeySpec = new SecretKeySpec(secKeyBytes, getEnvProperty("Cipher.secret.key.sepc.algorithm"));
        Cipher cipher = Cipher.getInstance(getEnvProperty("Cipher.intance.algorithm"));
        IvParameterSpec iv = new IvParameterSpec(secKey.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, secKeySpec, iv);
        //先用base64解密
        return new String(cipher.doFinal(new BASE64Decoder().decodeBuffer(plainText)));
//        return parseByte2HexStr(new BASE64Decoder().decodeBuffer(plainText));
    }

    //AES加解密算法
    public String encrypt(String plainText, String secKey) throws Exception {
        if (checkSecKey(secKey)) {
            throw new ServiceException("error sec key");
        }
        byte[] secKeyBytes = secKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secKeySpec = new SecretKeySpec(secKeyBytes, getEnvProperty("Cipher.secret.key.sepc.algorithm"));
        Cipher cipher = Cipher.getInstance(getEnvProperty("Cipher.intance.algorithm"));//"算法/模式/补码方式"
        IvParameterSpec iv = new IvParameterSpec(secKey.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, secKeySpec, iv);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        return new BASE64Encoder().encode(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
//        return parseByte2HexStr(encrypted);
    }

    private boolean checkSecKey(String secKey) {
        if (secKey == null) {
            System.out.print("Key为空null");
            return true;
        }
        // 判断Key是否为16位
        if (secKey.length() != 16) {
            System.out.print("Key长度不是16位");
            return true;
        }
        return false;
    }

    public String encrypt(String plaintext, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(getEnvProperty("Cipher.intance.algorithm"));
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(plaintext.getBytes());
        return Base64.encodeBase64String(cipherText);
    }

    public String decrypt(String encryptedText, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(getEnvProperty("Cipher.intance.algorithm"));
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.decodeBase64(encryptedText));
        return new String(plainText);
    }

    /**
     * IV是伪随机值，其大小与加密的块相同。我们可以使用SecureRandom类生成随机IV。
     * @return
     * @throws NoSuchAlgorithmException
     */
    public IvParameterSpec generateIv() throws NoSuchAlgorithmException {
        byte[] iv = new byte[16];
        SecureRandom secureRandom = SecureRandom.getInstance(getEnvProperty("Cipher.salt.algorithm"));
        secureRandom.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

/*    private SecretKey getSecretKey(String plaintext) throws Exception {
        byte[] saltBytes = getSalt().getBytes();
        SecretKeyFactory skf = SecretKeyFactory.getInstance(getEnvProperty("Cipher.secret.key.algorithm"));
        PBEKeySpec spec = new PBEKeySpec(plaintext.toCharArray(), saltBytes, Integer.parseInt(getEnvProperty("Cipher.secret.key.sepc.iterations")), Integer.parseInt(getEnvProperty("Cipher.secret.key.sepc.size")));
        return skf.generateSecret(spec);
    }*/

    /**
     * 为了生成密钥，我们可以使用KeyGenerator类。让我们定义一种用于生成大小为n（128、192和256）位的AES密钥的方法
     * @param n
     * @return
     * @throws NoSuchAlgorithmException
     */
    public SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(getEnvProperty("Cipher.secret.key.sepc.algorithm"));
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    /**
     * 生成加密秘钥
     * @param password 加密的密码
     * @return SecretKeySpec
     * @author 溪云阁
     */
    private SecretKeySpec getSecretKey(final String password) throws NoSuchAlgorithmException {
        // 返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = KeyGenerator.getInstance(getEnvProperty("Cipher.secret.key.sepc.algorithm"));
        // AES 要求密钥长度为 128
        kg.init(Integer.parseInt(getEnvProperty("Cipher.secret.key.sepc.size")), new SecureRandom(password.getBytes()));
        // 生成一个密钥
        final SecretKey secretKey = kg.generateKey();
        // 转换为AES专用密钥
        return new SecretKeySpec(secretKey.getEncoded(), getEnvProperty("Cipher.secret.key.sepc.algorithm"));
    }


    /**
     * 第二种方法中，可以使用基于密码的密钥派生功能（例如PBKDF2）从给定的密码派生AES秘密密钥。我们还需要一个盐值来将密码转换为密钥。盐也是一个随机值。
     * <p>
     * 我们可以将SecretKeyFactory类与PBKDF2WithHmacSHA256算法一起使用，以根据给定的密​​码生成密钥。
     * <p>
     * 让我们定义一种方法，该方法可通过65,536次迭代和256位密钥长度从给定密码生成AES密钥
     * @param plaintext
     * @param salt
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public SecretKey getKeyFromPlaintext(String plaintext, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(getEnvProperty("Cipher.secret.key.algorithm"));
        KeySpec spec = new PBEKeySpec(plaintext.toCharArray(), salt.getBytes(), Integer.parseInt(getEnvProperty("Cipher.secret.key.sepc.iterations")), Integer.parseInt(getEnvProperty("Cipher.secret.key.sepc.size")));
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), getEnvProperty("Cipher.secret.key.sepc.algorithm"));
        return secret;
    }

    private String getEnvProperty(String key) {
        String property = env.getProperty(key);
        LOGGER.info("Key:{},Property:{}", key, property);
        return property;
    }

    /**
     * Gets salt. 获取密码对应的盐
     * @return the salt
     * @throws Exception the exception
     */
    public String getSalt() throws Exception {
        SecureRandom sr = SecureRandom.getInstance(getEnvProperty("Cipher.salt.algorithm"));
        byte[] salt = new byte[20];
        sr.nextBytes(salt);
        return Base64.encodeBase64String(salt);
    }


    /**
     * 将16进制转换为二进制
     * @param hexStr
     * @return
     */
    public byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 将二进制转换成16进制
     * @param buf
     * @return
     */
    public String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }
}
