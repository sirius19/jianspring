package com.jianspring.start.utils.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PassUtil 工具类的单元测试
 */
public class PassUtilTest {

    @Test
    @DisplayName("测试密码长度符合要求")
    void testPasswordLength() {
        int expectedLength = 8;
        String password = PassUtil.initPass(expectedLength);
        assertEquals(expectedLength, password.length(), "生成的密码长度应该与指定长度相同");

        expectedLength = 16;
        password = PassUtil.initPass(expectedLength);
        assertEquals(expectedLength, password.length(), "生成的密码长度应该与指定长度相同");
    }

    @Test
    @DisplayName("测试密码包含必要字符类型")
    void testPasswordContainsRequiredCharTypes() {
        String password = PassUtil.initPass(10);

        // 检查是否包含大写字母
        assertTrue(password.matches(".*[A-Z].*"), "密码应该包含至少一个大写字母");

        // 检查是否包含小写字母
        assertTrue(password.matches(".*[a-z].*"), "密码应该包含至少一个小写字母");

        // 检查是否包含数字
        assertTrue(password.matches(".*[0-9].*"), "密码应该包含至少一个数字");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 8, 12, 16, 20})
    @DisplayName("测试不同长度的密码生成")
    void testDifferentLengthPasswords(int length) {
        String password = PassUtil.initPass(length);
        assertEquals(length, password.length(), "生成的密码长度应该与指定长度相同");
        assertTrue(password.matches(".*[A-Z].*"), "密码应该包含至少一个大写字母");
        assertTrue(password.matches(".*[a-z].*"), "密码应该包含至少一个小写字母");
        assertTrue(password.matches(".*[0-9].*"), "密码应该包含至少一个数字");
    }

    @Test
    @DisplayName("测试密码唯一性")
    void testPasswordUniqueness() {
        String password1 = PassUtil.initPass(10);
        String password2 = PassUtil.initPass(10);

        assertNotEquals(password1, password2, "两次生成的密码应该不同");
    }

    @Test
    @DisplayName("测试密码长度小于4时抛出异常")
    void testInvalidPasswordLength() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PassUtil.initPass(3);
        }, "当密码长度小于4时应该抛出异常");

        String expectedMessage = "密码的位数不能小于3位";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), "异常消息应该包含预期的错误信息");
    }
}