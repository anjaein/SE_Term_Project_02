package com.issuetracker.global.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {
    private final Validator validator = new Validator() {};

    @Test
    @DisplayName("checkNonNull 통과: 모든 파라미터가 non-null이면 null을 반환한다")
    void checkNonNullPassesWhenAllPresent() {
        // when
        String result = validator.checkNonNull(1L, "title", 2L);

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("checkNonNull 실패: 하나라도 null이면 누락 메시지를 반환한다")
    void checkNonNullFailsWhenAnyNull() {
        // when
        String result = validator.checkNonNull(1L, null, 2L);

        // then
        assertNotNull(result);
        assertTrue(result.contains("missing"));
    }

    @Test
    @DisplayName("checkNonNull 통과: 빈 varargs는 null을 반환한다")
    void checkNonNullPassesForEmptyArgs() {
        // when
        String result = validator.checkNonNull();

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("checkNonBlank 통과: 정상 문자열이면 null을 반환한다")
    void checkNonBlankPassesForValidString() {
        // when
        String result = validator.checkNonBlank("hello", "fieldName");

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("checkNonBlank 실패: null이면 필드명을 포함한 메시지를 반환한다")
    void checkNonBlankFailsForNull() {
        // when
        String result = validator.checkNonBlank(null, "fieldName");

        // then
        assertNotNull(result);
        assertTrue(result.contains("fieldName"));
    }

    @Test
    @DisplayName("checkNonBlank 실패: 공백만 있는 문자열이면 필드명을 포함한 메시지를 반환한다")
    void checkNonBlankFailsForWhitespace() {
        // when
        String result = validator.checkNonBlank("   ", "fieldName");

        // then
        assertNotNull(result);
        assertTrue(result.contains("fieldName"));
    }
}
