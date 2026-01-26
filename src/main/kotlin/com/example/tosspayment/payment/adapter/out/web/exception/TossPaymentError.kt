package com.example.tosspayment.payment.adapter.out.web.exception

enum class TossPaymentError(val description: String) {
    // 성공
    DONE("결제 승인 성공"),

    // 재시도 가능한 에러 (Unknown 처리)
    PROVIDER_ERROR("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    FAILED_INTERNAL_SYSTEM_PROCESSING("내부 시스템 처리 중 오류가 발생했습니다."),
    UNKNOWN_PAYMENT_ERROR("알 수 없는 결제 오류입니다."),
    FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING("결제 내부 시스템 처리 중 오류가 발생했습니다."),

    // 결제 승인 실패 - 카드 관련
    REJECT_CARD_COMPANY("카드사에서 결제를 거절했습니다."),
    EXCEED_MAX_CARD_INSTALLMENT_PLAN("최대 할부 개월 수를 초과했습니다."),
    NOT_ALLOWED_CARD_COMPANY("허용되지 않는 카드사입니다."),
    INVALID_CARD_NUMBER("잘못된 카드 번호입니다."),
    INVALID_CARD_EXPIRY("잘못된 카드 유효기간입니다."),
    INVALID_STOPPED_CARD("정지된 카드입니다."),
    INVALID_CARD_LOST_OR_STOLEN("분실 또는 도난 카드입니다."),
    RESTRICTED_CARD("이용이 제한된 카드입니다."),
    EXCEED_MAX_AMOUNT("거래 금액 한도를 초과했습니다."),
    EXCEED_MAX_DAILY_PAYMENT_COUNT("일일 결제 한도 횟수를 초과했습니다."),
    EXCEED_MAX_MONTHLY_PAYMENT_COUNT("월별 결제 한도 횟수를 초과했습니다."),
    CARD_NOT_SUPPORTED_INSTALLMENT_PLAN("할부 이용이 불가한 카드입니다."),
    NOT_SUPPORTED_MONTHLY_INSTALLMENT_PLAN("월별 할부 이용이 불가합니다."),
    INVALID_CARD_INSTALLMENT_PLAN("잘못된 할부 개월 수입니다."),
    NOT_SUPPORTED_POINT_CARD("포인트 카드는 지원하지 않습니다."),
    NOT_ENOUGH_POINT("포인트가 부족합니다."),
    INVALID_CVV("CVV 값이 올바르지 않습니다."),
    INVALID_PASSWORD("카드 비밀번호가 올바르지 않습니다."),
    CARD_EXCEED_LIMIT("카드 사용 한도를 초과했습니다."),
    CARD_NOT_AVAILABLE("사용할 수 없는 카드입니다."),
    CARD_SYSTEM_ERROR("카드사 시스템 점검 중입니다."),

    // 결제 승인 실패 - 결제 관련
    ALREADY_PROCESSED_PAYMENT("이미 처리된 결제입니다."),
    INVALID_PAYMENT_KEY("잘못된 결제 키입니다."),
    PAYMENT_NOT_FOUND("존재하지 않는 결제입니다."),
    BELOW_MINIMUM_AMOUNT("최소 결제 금액 미만입니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    NOT_FOUND_PAYMENT_SESSION("결제 세션을 찾을 수 없습니다."),
    DUPLICATED_ORDER_ID("중복된 주문 ID입니다."),
    INVALID_ORDER_ID("잘못된 주문 ID입니다."),
    INVALID_AMOUNT("결제 금액이 올바르지 않습니다."),
    PAYMENT_WAITING_FOR_DEPOSIT("입금 대기 중인 결제입니다."),
    PAYMENT_ALREADY_CANCELED("이미 취소된 결제입니다."),
    PAYMENT_EXPIRED("결제 유효 시간이 만료되었습니다."),
    NOT_CANCELABLE_PAYMENT("취소할 수 없는 결제입니다."),
    PAY_PROCESS_CANCELED("사용자가 결제를 취소했습니다."),
    PAY_PROCESS_ABORTED("결제 진행이 중단되었습니다."),

    // 결제 승인 실패 - 인증 관련
    UNAUTHORIZED_KEY("인증되지 않은 API 키입니다."),
    INVALID_API_KEY("API 키가 올바르지 않습니다."),
    FORBIDDEN_REQUEST("접근 권한이 없습니다."),
    UNAUTHENTICATED_KEY("인증되지 않은 키입니다."),

    // 결제 승인 실패 - 가상계좌 관련
    INVALID_BANK("유효하지 않은 은행입니다."),
    NOT_SUPPORTED_BANK("지원하지 않는 은행입니다."),
    INVALID_ACCOUNT_INFO_RE_REGISTER("계좌 정보가 유효하지 않습니다."),
    INVALID_VIRTUAL_ACCOUNT("유효하지 않은 가상계좌입니다."),

    // 결제 승인 실패 - 휴대폰 결제 관련
    NOT_SUPPORTED_CARRIER("지원하지 않는 통신사입니다."),
    NOT_ALLOWED_CARRIER_FOR_PAYMENT("결제가 허용되지 않은 통신사입니다."),
    EXCEED_MAX_MOBILE_PHONE_AMOUNT("휴대폰 결제 한도를 초과했습니다."),

    // 결제 승인 실패 - 간편결제 관련
    NOT_AVAILABLE_SIMPLE_PAYMENT("간편결제를 사용할 수 없습니다."),
    INVALID_SIMPLE_PAYMENT_INFO("간편결제 정보가 올바르지 않습니다."),

    // 결제 승인 실패 - 사용자 관련
    PAY_UNAUTHORIZED("결제 요청자와 계정이 일치하지 않습니다."),
    TOSS_APP_UPDATE_REQUIRED("토스 앱 업데이트가 필요합니다."),
    PAY_FRAUD_DETECTED("사기 거래가 감지되었습니다."),
    FRAUD_DETECTION_SYSTEM_ERROR("FDS 시스템 오류입니다."),

    // 결제 승인 실패 - 자동결제(빌링) 관련
    BILLING_KEY_NOT_FOUND("빌링키를 찾을 수 없습니다."),
    INVALID_BILLING_KEY("유효하지 않은 빌링키입니다."),
    BILLING_KEY_ALREADY_DELETED("이미 삭제된 빌링키입니다."),
    BILLING_KEY_EXPIRED("만료된 빌링키입니다."),

    // 결제 승인 실패 - 기타
    NOT_FOUND_TERMINAL_ID("터미널 ID를 찾을 수 없습니다."),
    INVALID_AUTHORIZE_AUTH("잘못된 인증 정보입니다."),
    INVALID_METADATA("잘못된 메타데이터입니다."),
    NOT_FOUND_PAYMENT("결제를 찾을 수 없습니다."),
    FAILED_TO_REFUND("환불에 실패했습니다."),
    NOT_REFUNDABLE_AMOUNT("환불 가능 금액을 초과했습니다."),

    // 알 수 없는 에러 (기본값)
    UNKNOWN("알 수 없는 에러입니다.");

    fun isSuccess(): Boolean {
        return when (this) {
            ALREADY_PROCESSED_PAYMENT -> true
            else -> false
        }
    }

    fun isFailure(): Boolean {
        return when (this) {
            ALREADY_PROCESSED_PAYMENT,
            UNKNOWN,
            UNKNOWN_PAYMENT_ERROR,
            PROVIDER_ERROR,
            FAILED_INTERNAL_SYSTEM_PROCESSING,
            FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING,
            CARD_SYSTEM_ERROR,
            FRAUD_DETECTION_SYSTEM_ERROR -> false

            else -> true
        }
    }

    fun isUnknown(): Boolean {
        return !isSuccess() && !isFailure()
    }

    fun isRetryableError(): Boolean {
        return isUnknown()
    }

    companion object {
        fun get(errorCode: String): TossPaymentError {
            return entries.find { it.name == errorCode } ?: UNKNOWN
        }
    }
}
