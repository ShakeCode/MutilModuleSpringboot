package com.task.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Result vo.
 * @param <T> the type parameter
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResultVO<T> {

    /**
     * The Code.
     */
    public int code;

    private String message;

    private T data;


    private static enum ResultCode {
        /**
         * Success result code.
         */
// 成功
        SUCCESS(200, "成功"),

        /**
         * Internal server error result code.
         */
// 服务器内部错误
        INTERNAL_SERVER_ERROR(500, "内部服务错误");

        private int code;
        private String message;

        ResultCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        /**
         * Gets code.
         * @return the code
         */
        public int getCode() {
            return code;
        }

        /**
         * Sets code.
         * @param code the code
         */
        public void setCode(int code) {
            this.code = code;
        }

        /**
         * Gets message.
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Sets message.
         * @param message the message
         */
        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Success result vo.
     * @return the result vo
     */
    public static ResultVO success() {
        return new ResultVO(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message, null);
    }

    /**
     * Success result vo.
     * @param message the message
     * @return the result vo
     */
    public static ResultVO success(String message) {
        return new ResultVO(ResultCode.SUCCESS.code, message, null);
    }

    /**
     * Success data result vo.
     * @param <T>  the type parameter
     * @param data the data
     * @return the result vo
     */
    public static <T> ResultVO<T> successData(T data) {
        return new ResultVO(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message, data);
    }

    /**
     * Fail result vo.
     * @param <T>     the type parameter
     * @param message the message
     * @return the result vo
     */
    public static <T> ResultVO<T> fail(String message) {
        return new ResultVO(ResultCode.INTERNAL_SERVER_ERROR.code, message, null);
    }

    /**
     * Fail result vo.
     * @param <T>     the type parameter
     * @param code    the code
     * @param message the message
     * @return the result vo
     */
    public static <T> ResultVO<T> fail(int code, String message) {
        return new ResultVO(code, message, null);
    }
}
