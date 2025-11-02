package com.application.expenseTracker.ExpenseTracker.Repository;

import com.application.expenseTracker.ExpenseTracker.Entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Repository
public interface CalculationRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCategory(Expense.ExpenseCategory category);
    List<Expense> findByPaymentMode(Expense.PaymentMode paymentMode);
    List<Expense> findByExpenseDoneBetween(LocalDate start, LocalDate end);
    List<Expense> findByExpenseDoneLessThanEqual(LocalDate now);

    @Query(value = "SELECT COALESCE(SUM(E.amount),0) FROM Expense E WHERE MONTH(E.expenseDone) = (MONTH(:now)) AND YEAR(E.expenseDone) = (YEAR(:now))" )
    BigDecimal getTotalExpensesThisMonth(@Param("now") LocalDate now);

    @Query(value = "SELECT E.category, SUM(E.amount) AS TOTAL FROM Expense E GROUP BY E.category ORDER BY TOTAL DESC")
    List<Object[]> getTopCategories();

    @Query(value = "SELECT SUM(E.amount)/COUNT(DISTINCT E.expenseDone) FROM Expense E WHERE MONTH(E.expenseDone) = (MONTH(:now)) AND YEAR(E.expenseDone) = (YEAR(:now))")
    BigDecimal getAverageDailyExpense(@Param("now") LocalDate now);

    @Query(value = "FROM Expense E WHERE E.isRecurring = true")
    List<Expense> getRecurringExpenses();
}
