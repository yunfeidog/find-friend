package com.yunfei.ikunfriend.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
// callSuper = true表示在生成的equals和hashCode方法中调用父类的equals和hashCode方法
public class TeamQueryDTO extends PageDTO {

    private Long id;
    private String name;

    private String description;

    private Integer maxNum;

    private Long userId;

    private Integer status;

    private String searchText;
    /**
     * id 列表
     */
    private List<Long> idList;

}
