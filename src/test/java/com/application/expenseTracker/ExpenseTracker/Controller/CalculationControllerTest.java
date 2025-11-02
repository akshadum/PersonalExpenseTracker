package com.application.expenseTracker.ExpenseTracker.Controller;

import com.application.expenseTracker.ExpenseTracker.Entity.Expense;
import com.application.expenseTracker.ExpenseTracker.Service.CalculationService;
import com.application.expenseTracker.ExpenseTracker.Service.EmailService;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class CalculationControllerTest {

    @Mock
    private CalculationService calculationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CalculationController calculationController;

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

    /* TEST CASES REGARDING ADDED EXPENSES */
    @Test
    public void testAddExpense_Success() {
        doNothing().when(calculationService).addExpense(testExpense);
        doNothing().when(calculationService).checkAndSendBudgetAlerts(anyString(), anyInt(), any());

        ResponseEntity<?> entity = calculationController.addExpense(testExpense);

        assertEquals(200, entity.getStatusCodeValue());
        verify(calculationService, times(1)).checkAndSendBudgetAlerts(anyString(), anyInt(), any());
        verify(calculationService, times(1)).addExpense(testExpense);
    }

    @Test
    public void testAddExpense_MissingEmail() {
        testExpense.setUserEmail("");
        ResponseEntity<?> entity = calculationController.addExpense(testExpense);

        assertEquals(400, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("Email-Id"));
        verify(calculationService, times(0)).addExpense(testExpense);
    }

    @Test
    public void testAddExpense_InvalidBudget() {
        testExpense.setBudget(0);
        ResponseEntity<?> entity = calculationController.addExpense(testExpense);

        assertEquals(400, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("Budget"));
        verify(calculationService, times(0)).addExpense(testExpense);
    }

    @Test
    void testAddExpense_ExceptionHandling() {
        doThrow(new RuntimeException()).when(calculationService).addExpense(testExpense);
        ResponseEntity<?> entity = calculationController.addExpense(testExpense);

        assertEquals(500, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("An error occurred"));
        verify(calculationService, times(1)).addExpense(testExpense);
    }

    /* TEST CASES REGARDING FETCHING EXPENSES */

    @Test
    void testGetAllExpense_WithResults() {
        when(calculationService.getAllExpense()).thenReturn(Arrays.asList(testExpense));

        ResponseEntity<?> entity = calculationController.getAllExpense();

        assertEquals(200, entity.getStatusCodeValue());
        assertNotNull(entity.getBody());
        verify(calculationService, times(1)).getAllExpense();
    }

    @Test
    void testGetAllExpense_EmptyList() {
        when(calculationService.getAllExpense()).thenReturn(null);

        ResponseEntity<?> entity = calculationController.getAllExpense();

        assertEquals(204, entity.getStatusCodeValue());
        assertNull(entity.getBody());
        verify(calculationService, times(1)).getAllExpense();
    }

    @Test
    void testGetAllExpense_ExceptionHandling() {
        when(calculationService.getAllExpense()).thenThrow(new RuntimeException());

        ResponseEntity<?> entity = calculationController.getAllExpense();

        assertEquals(500, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("An error occurred"));
        verify(calculationService, times(1)).getAllExpense();
    }

    /* TEST CASES REGARDING UPDATING EXPENSES */
    @Test
    void testUpdateExpense_Success() {
        when(calculationService.findById(1L)).thenReturn(Optional.of(testExpense));
        doNothing().when(calculationService).updateExpense(testExpense);

        ResponseEntity<?> entity = calculationController.updateExpense(testExpense, 1L);

        assertEquals(200, entity.getStatusCodeValue());
        verify(calculationService, times(1)).updateExpense(testExpense);
    }

    @Test
    void testUpdateExpense_NotFound() {
        when(calculationService.findById(anyLong())).thenReturn(Optional.empty());
        doNothing().when(calculationService).updateExpense(testExpense);

        ResponseEntity<?> entity = calculationController.updateExpense(testExpense, 1L);

        assertEquals(500, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("Expense not found"));

        verify(calculationService, times(1)).findById(1L);
        verify(calculationService, times(0)).updateExpense(testExpense);
    }

    @Test
    void testUpdateExpense_ExceptionHandling() {
        when(calculationService.findById(anyLong())).thenReturn(Optional.of(testExpense));
        doThrow(new RuntimeException()).when(calculationService).updateExpense(testExpense);

        ResponseEntity<?> entity = calculationController.updateExpense(testExpense, anyLong());

        assertEquals(500, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("An error occurred"));

        verify(calculationService,times(1)).findById(anyLong());
        verify(calculationService,times(1)).updateExpense(testExpense);
    }
}

