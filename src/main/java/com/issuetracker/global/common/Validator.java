package com.issuetracker.global.common;

// 도메인 공통으로 필요한 검증 인터페이스
// 각 도메인에서는 추가로 필요한 검증 메서드를 별도로 작성
// Response 응답 형식 반환을 위해서 실패 시 message 반환
// 공통 검증 메서드 제공이어서 Interface와 default 메서드 사용
public interface Validator {

    // 파라미터가 null이 아닌지 검증
    default String checkNonNull(Object... values) {
        for (Object value : values) {
            if (value == null) {
                return "Required parameter is missing.";
            }
        }
        return null;
    }

    // 문자열(제목, 본문 등)이 null이거나 공백인지 검증 (String 전용 검증)
    default String checkNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " cannot be empty.";
        }
        return null;
    }
}