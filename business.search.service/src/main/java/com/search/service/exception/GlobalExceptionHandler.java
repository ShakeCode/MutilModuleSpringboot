package com.search.service.exception;

import com.alibaba.fastjson.JSON;
import com.search.service.model.ResultVO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@ResponseBody
@ControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理全部异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultVO handleException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Exception ex) {
        LOGGER.error(">>GlobalExceptionHandler handleException:{},url:{}", ex.getLocalizedMessage(), httpServletRequest.getRequestURI());
        // printErrorMsg(httpServletResponse, ex);
        return ResultVO.fail(ex.getMessage());
    }

    private void printErrorMsg(HttpServletResponse httpServletResponse, Exception ex) {
        // 手动输出错误
        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        // 防止输出乱码
        httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        try (OutputStream os = httpServletResponse.getOutputStream()) {
            IOUtils.write(JSON.toJSONString(ResultVO.fail(ex.getMessage())), os);
            os.flush();
        } catch (IOException e) {
            LOGGER.error(">>io error:{}", e.getLocalizedMessage());
        }
    }
}
