package com.yunfei.ikunfriend.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageDTO implements Serializable {
    /**
     * 每页条数
     */
    protected int pageSize=10;
    /**
     * 当前页码
     */
    protected int pageNum=1;
}
