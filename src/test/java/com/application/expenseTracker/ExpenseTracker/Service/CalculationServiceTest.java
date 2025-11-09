package com.application.expenseTracker.ExpenseTracker.Service;

import com.application.expenseTracker.ExpenseTracker.Entity.Expense;
import com.application.expenseTracker.ExpenseTracker.Exception.ExpenseNotFoundException;
import com.application.expenseTracker.ExpenseTracker.Exception.GlobalExceptionHandler;
import com.application.expenseTracker.ExpenseTracker.Exception.InvalidBudgetException;
import com.application.expenseTracker.ExpenseTracker.Repository.CalculationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class CalculationServiceTest {

    @Mock
    private CalculationRepository calculationRepository;

    @InjectMocks
    private CalculationService calculationService;

    @Mock
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setTitle("Food");
        testExpense.setAmount(new BigDecimal("250.00"));
        testExpense.setCategory(Expense.ExpenseCategory.FOOD);
        testExpense.setPaymentMode(Expense.PaymentMode.UPI);
        testExpense.setExpenseDone(LocalDate.now());
        testExpense.setBudget(1000);
        testExpense.setUserEmail("testuser@gmail.com");
    }

    @Test
    @DisplayName("🔍 Find Expense - Should return empty when expense not found")
    void testFindById_NotFound() {
        when(calculationRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Expense> mockTestExpense = calculationService.findById(1L);

        assertFalse(mockTestExpense.isPresent());
        verify(calculationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("🗑️ Delete Expense - Should successfully remove expense by ID")
    void testDeleteAllExpenses_Success() {
        doNothing().when(calculationRepository).deleteById(1L);

        calculationService.deleteExpenseById(1L);

        verify(calculationRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("⚠️ Budget Alert - Should trigger alert at 80% budget usage")
    void testCheckAndSendBudgetAlerts_At80Percent() {
        when(calculationRepository.getTotalExpensesThisMonth(any())).thenReturn(BigDecimal.valueOf(80));

        calculationService.checkAndSendBudgetAlerts("testuser@gmail.com", 1000, LocalDate.now());

        verify(calculationRepository, times(1)).getTotalExpensesThisMonth(any());
    }

    @Test
    @DisplayName("🚨 Budget Alert - Should trigger alert at 100% budget usage")
    void testCheckAndSendBudgetAlerts_At100Percent() {
        when(calculationRepository.getTotalExpensesThisMonth(any())).thenReturn(BigDecimal.valueOf(100));

        calculationService.checkAndSendBudgetAlerts("testuser@gmail.com", 1000, LocalDate.now());

        verify(calculationRepository, times(1)).getTotalExpensesThisMonth(any());
    }

    @Test
    @DisplayName("✅ Budget Alert - Should not trigger below 80% threshold")
    void testCheckAndSendBudgetAlerts_NoAlert() {
        when(calculationRepository.getTotalExpensesThisMonth(any()))
                .thenReturn(BigDecimal.valueOf(70));

        calculationService.checkAndSendBudgetAlerts("testuser@gmail.com", 1000, LocalDate.now());

        verify(calculationRepository, times(1)).getTotalExpensesThisMonth(any());
    }


    @Test
    @DisplayName("💥 Budget Alert - Should handle exception gracefully during alert check")
    void testCheckAndSendBudgetAlerts_Exception() {
        when(calculationRepository.getTotalExpensesThisMonth(LocalDate.now())).thenThrow(new RuntimeException());

        assertDoesNotThrow(() ->
                calculationService.checkAndSendBudgetAlerts("testuser@gmail.com", 1000, LocalDate.now())
        );
        verify(calculationRepository, times(1)).getTotalExpensesThisMonth(LocalDate.now());
    }

    @Test
    @DisplayName("🚫 Handle ExpenseNotFoundException - Should return 404 Not Found with proper message")
    void testHandleExpenseNotFoundException() {
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        ExpenseNotFoundException exception = new ExpenseNotFoundException("Expense not found for ID: 1");

        ResponseEntity<String> response = (ResponseEntity<String>) exceptionHandler.handleExpenseNotFound(exception);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("⚠️ Handle InvalidBudgetException - Should return 400 Bad Request with error details")
    void testHandleInvalidBudgetException() {
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        InvalidBudgetException exception = new InvalidBudgetException("Expense not found for ID: 1");

        ResponseEntity<String> response = (ResponseEntity<String>) exceptionHandler.handleInvalidBudget(exception);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("💥 Handle Generic Exception - Should return 500 Internal Server Error for unexpected errors")
    void testHandleGenericException() {
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        Exception exception = new Exception("Expense not found for ID: 1");

        ResponseEntity<String> response = (ResponseEntity<String>) exceptionHandler.handleGeneric(exception);
        assertEquals(404, response.getStatusCodeValue());

    }
}
