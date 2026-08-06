package com.rbac.util;

import com.rbac.common.Constants;
import com.rbac.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 密码安全工具类（等保2.0参考）
 * 1. 密码最小长度8位
 * 2. 复杂度校验：大小写、数字、特殊符号至少包含两类
 * 3. 内置弱口令黑名单拦截
 */
@Component
public class PwdSecurityUtil {

    /** 弱口令黑名单 */
    private static final Set<String> WEAK_PASSWORDS = new HashSet<>(Arrays.asList(
            "qwe123456", "admin123", "password123", "12345678",
            "qwerty123", "abc12345", "pass1234", "test1234",
            "admin@123", "p@ssw0rd", "welcome1", "letmein1",
            "a12345678", "admin1234", "root1234", "user1234",
            "password1", "iloveyou1", "sunshine1", "princess1"
    ));

    /**
     * 校验密码强度，不通过抛出BusinessException
     * @param password 明文密码
     */
    public static void validatePassword(String password) {
        if (password == null || password.length() < Constants.PWD_MIN_LENGTH) {
            throw new BusinessException("密码长度不能少于" + Constants.PWD_MIN_LENGTH + "位");
        }

        // 计算字符类型种类数
        int typeCount = 0;
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        if (hasUpper) typeCount++;
        if (hasLower) typeCount++;
        if (hasDigit) typeCount++;
        if (hasSpecial) typeCount++;

        // 至少包含两类字符
        if (typeCount < 2) {
            throw new BusinessException("密码必须包含大写字母、小写字母、数字、特殊符号中的至少两类");
        }

        // 弱口令黑名单检查（忽略大小写）
        if (WEAK_PASSWORDS.contains(password.toLowerCase())) {
            throw new BusinessException("密码过于简单，属于弱口令黑名单，请更换更复杂的密码");
        }
    }

    /**
     * 判断密码是否合法（不抛异常，返回boolean）
     */
    public static boolean isPasswordValid(String password) {
        try {
            validatePassword(password);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }
}
