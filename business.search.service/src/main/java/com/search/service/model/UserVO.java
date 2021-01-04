package com.search.service.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UserVO {
    @ApiModelProperty("标签用户")
    private String tags;

    @ApiModelProperty("用户号码")
    private String userId;

    @ApiModelProperty("经纬度坐标")
    private String location;

    @ApiModelProperty("计划发送时间")
    private String planSendTime;
}
