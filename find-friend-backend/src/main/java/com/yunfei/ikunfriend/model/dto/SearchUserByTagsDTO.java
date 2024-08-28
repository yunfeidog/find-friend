package com.yunfei.ikunfriend.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchUserByTagsDTO {
    private List<String> tagNameList;
}
