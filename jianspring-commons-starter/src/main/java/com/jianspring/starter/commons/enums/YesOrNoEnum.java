package com.jianspring.starter.commons.enums;

import lombok.Getter;
import lombok.ToString;

/**
 * @Author:  InfoInsights
 * @Date: 2023/3/7 下午3:35
 * @Version: 1.0.0
 */
@Getter
@ToString
public enum YesOrNoEnum {

    YES(1, "是"),
    NO(0, "否"),
    ;

    private final Integer value;

    private final String desc;

    YesOrNoEnum(Integer value, String desc){
       this.value = value;
       this.desc = desc;
    }

}