package com.example.tosspayment.payment.adapter.out.web.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.OffsetDateTime

/**
 * 토스페이먼츠 결제 승인 응답 (Payment 객체)
 * @see https://docs.tosspayments.com/reference#payment-객체
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TossPaymentConfirmationResponse(
    // 필수 필드
    val paymentKey: String,
    val orderId: String,
    val orderName: String,
    val status: String,
    val requestedAt: OffsetDateTime,
    val approvedAt: OffsetDateTime?,
    val totalAmount: Long,
    val balanceAmount: Long,
    val suppliedAmount: Long,
    val vat: Long,
    val method: String?,
    val type: String,

    // 카드 결제 정보
    val card: Card? = null,

    // 가상계좌 정보
    val virtualAccount: VirtualAccount? = null,

    // 계좌이체 정보
    val transfer: Transfer? = null,

    // 휴대폰 결제 정보
    val mobilePhone: MobilePhone? = null,

    // 간편결제 정보
    val easyPay: EasyPay? = null,

    // 현금영수증 정보
    val cashReceipt: CashReceipt? = null,
    val cashReceipts: List<CashReceipt>? = null,

    // 할인 정보
    val discount: Discount? = null,

    // 취소 정보
    val cancels: List<Cancel>? = null,

    // 실패 정보
    val tossFailureResponse: TossFailureResponse? = null,

    // 기타 필드
    val mId: String? = null,
    val version: String? = null,
    val lastTransactionKey: String? = null,
    val currency: String = "KRW",
    val country: String = "KR",
    val taxFreeAmount: Long = 0,
    val taxExemptionAmount: Long = 0,
    val cultureExpense: Boolean = false,
    val isPartialCancelable: Boolean = true,

    // 원본 JSON (디버깅/로깅용)
    val receipt: Receipt? = null,
    val checkout: Checkout? = null
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Card(
        val issuerCode: String?,
        val acquirerCode: String?,
        val number: String?,
        val installmentPlanMonths: Int,
        val isInterestFree: Boolean,
        val interestPayer: String?,
        val approveNo: String?,
        val useCardPoint: Boolean,
        val cardType: String?,
        val ownerType: String?,
        val acquireStatus: String?,
        val amount: Long?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class VirtualAccount(
        val accountType: String?,
        val accountNumber: String?,
        val bankCode: String?,
        val customerName: String?,
        val dueDate: String?,
        val refundStatus: String?,
        val expired: Boolean?,
        val settlementStatus: String?,
        val refundReceiveAccount: RefundReceiveAccount?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RefundReceiveAccount(
        val bankCode: String?,
        val accountNumber: String?,
        val holderName: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Transfer(
        val bankCode: String?,
        val settlementStatus: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MobilePhone(
        val customerMobilePhone: String?,
        val settlementStatus: String?,
        val receiptUrl: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class EasyPay(
        val provider: String?,
        val amount: Long?,
        val discountAmount: Long?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CashReceipt(
        val type: String?,
        val receiptKey: String?,
        val issueNumber: String?,
        val receiptUrl: String?,
        val amount: Long?,
        val taxFreeAmount: Long?,
        val issueStatus: String?,
        val failureCode: String?,
        val failureMessage: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Discount(
        val amount: Long?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Cancel(
        val cancelAmount: Long?,
        val cancelReason: String?,
        val taxFreeAmount: Long?,
        val taxExemptionAmount: Long?,
        val refundableAmount: Long?,
        val easyPayDiscountAmount: Long?,
        val canceledAt: OffsetDateTime?,
        val transactionKey: String?,
        val receiptKey: String?,
        val cancelStatus: String?,
        val cancelRequestId: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TossFailureResponse(
        val code: String?,
        val message: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Receipt(
        val url: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Checkout(
        val url: String?
    )
}
