package com.search.service.controller;

import com.search.service.model.ResultVO;
import com.search.service.security.CipherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("v1/aes/")
@Api("AES-128位对称加密算法")
@RestController
public class AesCipherController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AesCipherController.class);

    @Value("${secKey:1234567890abcdef}")
    private String secKey;

    private final CipherService cipherService;

    public AesCipherController(CipherService cipherService) {
        this.cipherService = cipherService;
    }

    @ApiOperation("AES-128位对称加密算法-加密")
    @GetMapping("/encrypt")
    public ResultVO<String> encryptData(@RequestParam("param") String param) throws Exception {
        String encryptStr = cipherService.encrypt(param, secKey);
//        String encryptStr = AesUtils.encrypt(param, secKey);
//        String decryptResult = AesUtils.decrypt(encryptStr, secKey);
        LOGGER.info("encryptStr:{}", encryptStr);
        String decryptStr = cipherService.decrypt(encryptStr, secKey);
        LOGGER.info("decryptStr:{}", decryptStr);
        return ResultVO.successData(encryptStr);
    }

    @ApiOperation("AES-128位对称加密算法-解密")
    @GetMapping("/decrypt")
    public ResultVO<String> decryptData(@RequestParam("param") String param) throws Exception {
        String decryptStr = cipherService.decrypt(param, secKey);
        LOGGER.info("decryptStr:{}", decryptStr);
        return ResultVO.successData(decryptStr);
    }
}
