package com.yunfei.ikunfriend.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchUserByTagsDto {
    private List<String> tagNameList;
}
