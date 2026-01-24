package com.example.tosspayment.payment.adapter.out.persistence

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

enum class PaymentType(description: String) {
    NORMAL("일반 결제")
}

enum class PaymentMethod(description: String) {
    EASY_PAY("간편 결제")
}

enum class PaymentStatus(description: String) {
    NOT_STARTED("결제 승인 시작 전"),
    EXECUTING("결제 승인 중"),
    SUCCESS("결제 승인 성공"),
    FAILURE("결제 승인 실패"),
    UNKNOWN("결제 승인 알 수 없는 상태")
}

object PaymentEvents : Table("payment_events") {
    val id = long("id").autoIncrement()
    val buyerId = long("buyer_id")
    val isPaymentDone = bool("is_payment_done").default(false)
    val paymentKey = varchar("payment_key", 255).uniqueIndex().nullable()
    val orderId = varchar("order_id", 255).uniqueIndex().nullable()
    val type = enumerationByName<PaymentType>("type", 10)
    val method = enumerationByName<PaymentMethod>("method", 10).nullable()
    val pspRawData = text("psp_raw_data").nullable()
    val approvedAt = datetime("approved_at").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object PaymentOrders : Table("payment_orders") {
    val id = long("id").autoIncrement()
    val paymentEventId = long("payment_event_id").references(PaymentEvents.id)
    val sellerId = long("seller_id")
    val productId = long("product_id")
    val orderId = varchar("order_id", 255)
    val amount = decimal("amount", 12, 2)
    val paymentOrderStatus = enumerationByName<PaymentStatus>("payment_order_status", 20)
        .default(PaymentStatus.NOT_STARTED)
    val ledgerUpdated = bool("ledger_updated").default(false)
    val walletUpdated = bool("wallet_updated").default(false)
    val failedCount = byte("failed_count").default(0)
    val threshold = byte("threshold").default(5)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object PaymentOrderHistories : Table("payment_order_histories") {
    val id = long("id").autoIncrement()
    val paymentOrderId = long("payment_order_id").references(PaymentOrders.id)
    val previousStatus = enumerationByName<PaymentStatus>("previous_status", 20).nullable()
    val newStatus = enumerationByName<PaymentStatus>("new_status", 20).nullable()
    val createdAt = datetime("created_at")
    val changedBy = varchar("changed_by", 255).nullable()
    val reason = varchar("reason", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}
