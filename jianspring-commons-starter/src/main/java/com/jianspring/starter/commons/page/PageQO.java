package com.jianspring.starter.commons.page;

import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Setter
@ToString
public class PageQO<T> {

    private Integer pageNum;

    private Integer pageSize;

    private List<Sort> sorts;

    private T query;

    public T getQuery() {
        return query;
    }

    public Integer getPageSize() {
        if (pageSize > 10000) {
            return 10000;
        }
        return pageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    @Setter
    @ToString
    public static class Sort {

        private String sortName;

        private String orderBy;

        public String getSortName() {
            if (null == sortName || "".equals(sortName)) {
                return null;
            }
            return sortName.trim();
        }

        public String getOrderBy() {
            if (null == orderBy || "".equals(orderBy)) {
                orderBy = "asc";
            }
            if (!"asc".equals(orderBy) && !"desc".equals(orderBy)) {
                return "desc";
            }
            return orderBy;
        }
    }

}