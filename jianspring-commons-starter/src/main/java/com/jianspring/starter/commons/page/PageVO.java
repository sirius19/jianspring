package com.jianspring.starter.commons.page;

import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
@ToString
public class PageVO<T> {

    private Integer pageNum;

    private Integer pageSize;

    private Long total;

    private List<T> data;

    public static <T> PageVO<T> of(Integer pageNum, Integer pageSize, Long total, List<T> data) {
        PageVO<T> pageVO = new PageVO<>();
        if (null == pageNum || pageNum < 1) {
            pageNum = 1;
        }
        if (null == pageSize) {
            pageSize = 10;
        }
        if (null == total) {
            total = 0L;
        }
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotal(total);
        pageVO.setData(data);
        return pageVO;
    }

}