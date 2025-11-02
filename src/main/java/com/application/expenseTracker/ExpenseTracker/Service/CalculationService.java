package com.application.expenseTracker.ExpenseTracker.Service;

import com.application.expenseTracker.ExpenseTracker.Controller.CalculationController;
import com.application.expenseTracker.ExpenseTracker.Entity.Expense;
import com.application.expenseTracker.ExpenseTracker.Repository.CalculationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CalculationService {

    private static final Logger logger = LoggerFactory.getLogger(CalculationController.class);

    @Autowired
    public CalculationRepository calculationRepository;

    @Autowired
    private EmailService emailService;


    public List<Expense> getAllExpense() {
        return calculationRepository.findAll();
    }

    public void updateExpense(Expense expense) {
        calculationRepository.save(expense);
    }

    public void addExpense(Expense expense) {
        calculationRepository.save(expense);
    }

    public Optional<Expense> findById(Long id){
        return calculationRepository.findById(id);
    }

    public List<Expense> getExpensesByCategory(Expense.ExpenseCategory category) {
        return calculationRepository.findByCategory(category);
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate){
        return calculationRepository.findByExpenseDoneBetween(startDate,endDate);
    }

    public void deleteExpenseById(Long id) {
        calculationRepository.deleteById(id);
    }

    public void deleteAllExpenses() {
        calculationRepository.deleteAll();
    }

    public List<Expense> getExpensesByPaymentMode(Expense.PaymentMode paymentMode) {
        return calculationRepository.findByPaymentMode(paymentMode);
    }

    public List<Expense> getTotalExpenses(LocalDate now) {
        return calculationRepository.findByExpenseDoneLessThanEqual(now);
    }

    public List<Expense> getRecentExpenses(LocalDate now) {
        return calculationRepository.findByExpenseDoneBetween(now.minusDays(7),now);
    }

    public Map<String, Object> getExpenseSummary() {
        LocalDate now = LocalDate.now();
        Map<String, Object> summary = new LinkedHashMap<>();
        BigDecimal totalExpensesThisMonth = calculationRepository.getTotalExpensesThisMonth(now);
        Map<String,String> topCategories = (Map<String, String>) calculationRepository.getTopCategories();
        BigDecimal avgDailyExpenses = calculationRepository.getAverageDailyExpense(now);

        summary.put("totalExpensesThisMonth",totalExpensesThisMonth);
        summary.put("averageDailyExpenses",avgDailyExpenses);
        summary.put("top3Categories",topCategories);

        return summary;
    }

    public void checkAndSendBudgetAlerts(String userEmail ,int budget, LocalDate now){
        String subject = "";
        BigDecimal calculatedTotalExpense = calculationRepository.getTotalExpensesThisMonth(now);
        int percentage = calculatedTotalExpense
                .divide(BigDecimal.valueOf(budget), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        if(percentage >= 80 && percentage < 90) {
            subject = "⚠️ Budget Alert: 80% reached!";
        }else if(percentage >= 90 && percentage < 100) {
            subject = "🚨  Budget Alert: 90% reached!";
        }else if(percentage >= 100 && percentage < 120){
            subject = "❗Budget Limit Reached!";
        }else if(percentage >= 120){
            subject = "🔥 Overspent! 120% of Budget Crossed!";
        }

        if (!subject.isEmpty()) {
            String body = String.format(
                    "Hi User,%n%n" +
                            "You have spent ₹%.2f out of your ₹%.2f budget (%.0f%%).%n" +
                            "Please review your expenses.%n%n" +
                            "— Expense Tracker Team",
                    calculatedTotalExpense, budget, percentage);

            emailService.sendBudgetAlert(userEmail, subject, body);
        }

    }

    public List<Expense> getRecurringExpenses() {
        return calculationRepository.getRecurringExpenses();
    }

    public void updateExpenseDone(Expense expense, LocalDate date) {
        calculationRepository.save(expense);
    }
}
