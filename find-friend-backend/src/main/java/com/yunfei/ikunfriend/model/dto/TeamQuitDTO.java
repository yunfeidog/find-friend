package com.yunfei.ikunfriend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍
 * @TableName team
 */
@Data
public class TeamQuitDTO implements Serializable {
    /**
     * id
     */
    private Long teamId;

}