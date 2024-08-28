package com.yunfei.ikunfriend.model.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍
 * @TableName team
 */
@Data
public class TeamJoinDTO implements Serializable {
    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;


}