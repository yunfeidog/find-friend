package com.yunfei.ikunfriend.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用户常量 权限默认就是 public static final
 */
public interface UserConstant {
    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login_state";

    /**
     * 权限  0：普通用户  1：管理员
     */
    int DEFAULT_USER_ROLE = 0;

    int ADMIN_USER_ROLE = 1;

    /**
     * 随机坤头
     */
    List<String> AVATAR_URL = new ArrayList<>(Arrays.asList(
            "https://s2.loli.net/2023/10/30/rnR7x2BTU4tKL1S.webp",
            "https://s2.loli.net/2023/10/30/Bfc4xrEvWuPGYi3.webp",
            "https://s2.loli.net/2023/10/30/8npiBDWPZV1btOm.webp",
            "https://s2.loli.net/2023/10/30/zEOwXNJ7CUAVPWY.webp",
            "https://s2.loli.net/2023/10/30/XCABE7f4oRN2qIe.webp",
            "https://s2.loli.net/2023/10/30/6oWwuHQRsJKyzcB.webp",
            "https://s2.loli.net/2023/10/30/yj8slFt5L6xiS7z.webp"
    ));

}
