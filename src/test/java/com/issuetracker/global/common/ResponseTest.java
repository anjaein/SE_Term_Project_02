package com.issuetracker.global.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    @DisplayName("success(message, data): 성공 응답에 데이터가 담긴다")
    void successWithDataCarriesPayload() {
        // when
        Response<String> response = Response.success("OK", "payload");

        // then
        assertTrue(response.isSuccess());
        assertEquals("OK", response.getMessage());
        assertEquals("payload", response.getData());
    }

    @Test
    @DisplayName("success(message): 데이터 없는 성공 응답의 data는 null이다")
    void successWithoutDataHasNullPayload() {
        // when
        Response<String> response = Response.success("Done.");

        // then
        assertTrue(response.isSuccess());
        assertEquals("Done.", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("fail(message): 실패 응답은 success=false이고 data가 null이다")
    void failHasNullPayloadAndFalseSuccess() {
        // when
        Response<String> response = Response.fail("Something went wrong.");

        // then
        assertFalse(response.isSuccess());
        assertEquals("Something went wrong.", response.getMessage());
        assertNull(response.getData());
    }
}
