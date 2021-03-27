package com.data.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResultVO<T> {

    public int code;

    private String message;

    private T data;


    private static enum ResultCode {
        // 成功
        SUCCESS(200, "成功"),

        // 服务器内部错误
        INTERNAL_SERVER_ERROR(500, "内部服务错误");

        private int code;
        private String message;

        ResultCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static ResultVO success() {
        return new ResultVO(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message, null);
    }

    public static ResultVO success(String message) {
        return new ResultVO(ResultCode.SUCCESS.code, message, null);
    }

    public static <T> ResultVO<T> successData(T data) {
        return new ResultVO(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message, data);
    }

    public static <T> ResultVO<T> fail(String message) {
        return new ResultVO(ResultCode.INTERNAL_SERVER_ERROR.code, message, null);
    }

    public static <T> ResultVO<T> fail(int code, String message) {
        return new ResultVO(code, message, null);
    }

    public static void main(String[] args) {
        System.out.println(ResultVO.success());
    }
}
