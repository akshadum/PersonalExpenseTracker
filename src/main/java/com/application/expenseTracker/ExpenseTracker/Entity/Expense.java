package com.application.expenseTracker.ExpenseTracker.Entity;

import com.sun.istack.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.lang.annotation.Documented;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name="USER_EXPENSE",
        indexes = {
        @Index(name = "idx_payment_mode", columnList = "paymentMode"),
        @Index(name = "idx_category", columnList = "category")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Expense {

    public enum ExpenseCategory { FOOD, TRAVEL, RENT, UTILITIES, OTHER }
    public enum PaymentMode { CASH, CARD, UPI, NETBANKING }


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @NotNull
    private String title;
    private String userEmail;
    @NotNull
    private BigDecimal amount;
    private int budget;
    @NotNull
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;
    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;
    @NotNull
    private LocalDate expenseDone;
    private String notes;
    private boolean isRecurring;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
