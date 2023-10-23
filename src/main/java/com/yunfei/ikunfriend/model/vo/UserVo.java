package com.yunfei.ikunfriend.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class UserVO implements Serializable {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户账户
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;



    /**
     * 点话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 0-正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;



    /**
     * 用户角色
     */
    private Integer userRole;

    /**
     * ikun编号
     */
    private String ikunCode;

    /**
     * tags标签
     */
    private String tags;
    private static final long serialVersionUID = 1L;
}