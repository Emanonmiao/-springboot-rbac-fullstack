package com.rbac.util;

import com.rbac.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PwdSecurityUtil 密码安全工具类单元测试
 * JUnit5 + 参数化测试
 */
class PwdSecurityUtilTest {

    // ==================== 长度校验 ====================

    @Test
    @DisplayName("密码为null应抛出异常")
    void testNullPassword() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> PwdSecurityUtil.validatePassword(null));
        assertTrue(ex.getMessage().contains("8位"));
    }

    @Test
    @DisplayName("密码少于8位应抛出异常")
    void testShortPassword() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> PwdSecurityUtil.validatePassword("Ab1"));
        assertTrue(ex.getMessage().contains("8位"));
    }

    @Test
    @DisplayName("恰好8位的合法密码应通过")
    void testMinLengthPassword() {
        // Abc12345 在黑名单中，改用不在黑名单且满足复杂度的 8 位密码
        assertDoesNotThrow(() -> PwdSecurityUtil.validatePassword("Zx9@abcd"));
    }

    // ==================== 复杂度校验 ====================

    @Test
    @DisplayName("纯小写字母应抛出异常（只有一类字符）")
    void testOnlyLowercase() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> PwdSecurityUtil.validatePassword("abcdefgh"));
        assertTrue(ex.getMessage().contains("至少两类"));
    }

    @Test
    @DisplayName("纯数字应抛出异常")
    void testOnlyDigits() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> PwdSecurityUtil.validatePassword("12345678"));
        assertTrue(ex.getMessage().contains("至少两类"));
    }

    @Test
    @DisplayName("小写+数字（两类）应通过")
    void testLowerAndDigit() {
        assertDoesNotThrow(() -> PwdSecurityUtil.validatePassword("abcdef12"));
    }

    @Test
    @DisplayName("大写+小写+数字+特殊符号（四类）应通过")
    void testAllTypes() {
        assertDoesNotThrow(() -> PwdSecurityUtil.validatePassword("Admin@2024"));
    }

    // ==================== 弱口令黑名单 ====================

    @ParameterizedTest
    @ValueSource(strings = {"qwe123456", "Qwe123456", "QWE123456"})
    @DisplayName("Qwe123456各种大小写变体都应被拦截")
    void testWeakQwe123456(String pwd) {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> PwdSecurityUtil.validatePassword(pwd));
        assertTrue(ex.getMessage().contains("弱口令"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin123", "Admin123", "ADMIN123"})
    @DisplayName("Admin123各种大小写变体都应被拦截")
    void testWeakAdmin123(String pwd) {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> PwdSecurityUtil.validatePassword(pwd));
        assertTrue(ex.getMessage().contains("弱口令"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"password123", "Password123"})
    @DisplayName("password123应被拦截")
    void testWeakPassword123(String pwd) {
        assertThrows(BusinessException.class,
                () -> PwdSecurityUtil.validatePassword(pwd));
    }

    // ==================== isPasswordValid方法 ====================

    @Test
    @DisplayName("isPasswordValid对合法密码返回true")
    void testIsValidTrue() {
        assertTrue(PwdSecurityUtil.isPasswordValid("MyPass99"));
    }

    @Test
    @DisplayName("isPasswordValid对弱口令返回false")
    void testIsValidFalse() {
        assertFalse(PwdSecurityUtil.isPasswordValid("admin123"));
    }

    @Test
    @DisplayName("isPasswordValid对短密码返回false")
    void testIsValidShort() {
        assertFalse(PwdSecurityUtil.isPasswordValid("Ab1"));
    }
}
