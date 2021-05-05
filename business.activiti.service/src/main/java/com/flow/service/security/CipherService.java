package com.flow.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;

/**
 * 1、DSA加密
 * <p>
 * 2、RSA加密
 * <p>
 * 3、DES加密
 * 4、AES加密
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
 * The type Cipher util.
 */
@Service
public class CipherService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CipherService.class);

/*    private static int iterations = 65536;
    // 密钥长度128不需要特殊处理
    // 密钥长度是256需要特殊处理, 替换JCE jar包(local_policy.jar和US_export_policy.jar)
    private static int keySize = 128;
    private static byte[] ivBytes;
    private static SecretKey secretKey;*/

    private final Environment env;

    /**
     * Instantiates a new Cipher service.
     * @param env the env
     */
    public CipherService(Environment env) {
        this.env = env;
    }

    /**
     * Encrypt string.
     * @param plaintext the plaintext
     * @return the string
     * @throws Exception the exception
     */
    public String encrypt(String plaintext) throws Exception {
        SecretKey secretKey = getSecretKey(plaintext.toCharArray());
        SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), env.getProperty("Cipher.secret.key.spec.algorithm"));
        Cipher cipher = Cipher.getInstance(env.getProperty("Cipher.intance.algorithm"));
        cipher.init(Cipher.ENCRYPT_MODE, secretSpec);

        byte[] encryptedTextBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printBase64Binary(encryptedTextBytes);
    }

    private SecretKey getSecretKey(char[] plaintext) throws Exception {
        byte[] saltBytes = getSalt().getBytes();
        SecretKeyFactory skf = SecretKeyFactory.getInstance(env.getProperty("Cipher.secret.key.algorithm"));
        PBEKeySpec spec = new PBEKeySpec(plaintext, saltBytes, Integer.parseInt(env.getProperty("Cipher.secret.key.spec.iterations")), Integer.parseInt(env.getProperty("Cipher.secret.key.spec.size")));
        return skf.generateSecret(spec);
    }

    /**
     * Decrypt string.
     * @param encryptedText the encrypted text
     * @return the string
     * @throws Exception the exception
     */
    public String decrypt(String encryptedText) throws Exception {
        SecretKey secretKey = getSecretKey(encryptedText.toCharArray());
        byte[] encryptedTextBytes = DatatypeConverter.parseBase64Binary(encryptedText);
        SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), env.getProperty("Cipher.secret.key.spec.algorithm"));
        Cipher cipher = Cipher.getInstance(env.getProperty("Cipher.intance.algorithm"));
        AlgorithmParameters params = cipher.getParameters();
        byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();
        cipher.init(Cipher.DECRYPT_MODE, secretSpec, new IvParameterSpec(ivBytes));
        byte[] decryptedTextBytes;
        try {
            decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("decrypt text error", e);
            return null;
        }
        return new String(decryptedTextBytes,StandardCharsets.UTF_8);
    }

    /**
     * Gets salt.
     * @return the salt
     * @throws Exception the exception
     */
    public  String getSalt() throws Exception {
        SecureRandom sr = SecureRandom.getInstance(env.getProperty("Cipher.salt.algorithm"));
        byte[] salt = new byte[200];
        sr.nextBytes(salt);
        return new String(salt);
    }
}
