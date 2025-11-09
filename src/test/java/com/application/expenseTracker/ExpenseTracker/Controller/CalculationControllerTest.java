package com.application.expenseTracker.ExpenseTracker.Controller;

import com.application.expenseTracker.ExpenseTracker.Entity.Expense;
import com.application.expenseTracker.ExpenseTracker.Exception.ExpenseNotFoundException;
import com.application.expenseTracker.ExpenseTracker.Exception.GlobalExceptionHandler;
import com.application.expenseTracker.ExpenseTracker.Exception.InvalidBudgetException;
import com.application.expenseTracker.ExpenseTracker.Repository.CalculationRepository;
import com.application.expenseTracker.ExpenseTracker.Service.CalculationService;
import com.application.expenseTracker.ExpenseTracker.Service.EmailService;

import java.util.*;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("🧮 Calculation Controller Tests")
public class CalculationControllerTest {

    @Mock
    private CalculationService calculationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CalculationController calculationController;

    @Mock
    private CalculationRepository calculationRepository;

    @Mock
    private SessionFactory sessionFactory;

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
    @DisplayName("✅ Add Expense - Success Scenario")
    public void testAddExpense_Success() {
        doNothing().when(calculationService).addExpense(testExpense);
        doNothing().when(calculationService).checkAndSendBudgetAlerts(anyString(), anyInt(), any());

        ResponseEntity<?> entity = calculationController.addExpense(testExpense);

        assertEquals(200, entity.getStatusCodeValue());
        verify(calculationService, times(1)).checkAndSendBudgetAlerts(anyString(), anyInt(), any());
        verify(calculationService, times(1)).addExpense(testExpense);
    }

    @Test
    @DisplayName("🚫 Add Expense - Missing Email")
    public void testAddExpense_MissingEmail() {
        testExpense.setUserEmail("");
        ResponseEntity<?> entity = calculationController.addExpense(testExpense);

        assertEquals(400, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("Email-Id"));
        verify(calculationService, times(0)).addExpense(testExpense);
    }

    @Test
    @DisplayName("🚫 Add Expense - Invalid Budget")
    public void testAddExpense_InvalidBudget() {
        testExpense.setBudget(0);
        ResponseEntity<?> entity = calculationController.addExpense(testExpense);

        assertEquals(400, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("Budget"));
        verify(calculationService, times(0)).addExpense(testExpense);
    }

    @Test
    @DisplayName("💥 Add Expense - Exception Handling")
    void testAddExpense_ExceptionHandling() {
        doThrow(new RuntimeException()).when(calculationService).addExpense(testExpense);
        ResponseEntity<?> entity = calculationController.addExpense(testExpense);

        assertEquals(500, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("An error occurred"));
        verify(calculationService, times(1)).addExpense(testExpense);
    }

    /* TEST CASES REGARDING FETCHING EXPENSES */

    @Test
    @DisplayName("✅ Fetch All Expense - Success Scenario")
    void testGetAllExpense_WithResults() {
        when(calculationService.getAllExpense()).thenReturn(Arrays.asList(testExpense));

        ResponseEntity<?> entity = calculationController.getAllExpense();

        assertEquals(200, entity.getStatusCodeValue());
        assertNotNull(entity.getBody());
        verify(calculationService, times(1)).getAllExpense();
    }

    @Test
    @DisplayName("🚫 Fetch All Expense - Success Scenario")
    void testGetAllExpense_EmptyList() {
        when(calculationService.getAllExpense()).thenReturn(null);

        ResponseEntity<?> entity = calculationController.getAllExpense();

        assertEquals(204, entity.getStatusCodeValue());
        assertNull(entity.getBody());
        verify(calculationService, times(1)).getAllExpense();
    }

    @Test
    @DisplayName("💥 Get All Expenses - Should handle runtime exception")
    void testGetAllExpense_ExceptionHandling() {
        when(calculationService.getAllExpense()).thenThrow(new RuntimeException());

        ResponseEntity<?> entity = calculationController.getAllExpense();

        assertEquals(500, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("An error occurred"));
        verify(calculationService, times(1)).getAllExpense();
    }

    /* TEST CASES REGARDING UPDATING EXPENSES */
    @Test
    @DisplayName("✏️ Update Expense - Should update expense successfully")
    void testUpdateExpense_Success() {
        when(calculationService.findById(1L)).thenReturn(Optional.of(testExpense));
        doNothing().when(calculationService).updateExpense(testExpense);

        ResponseEntity<?> entity = calculationController.updateExpense(testExpense, 1L);

        assertEquals(200, entity.getStatusCodeValue());
        verify(calculationService, times(1)).updateExpense(testExpense);
    }

    @Test
    @DisplayName("🚫 Update Expense - Should return error when expense not found")
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
    @DisplayName("💥 Update Expense - Should handle exception during update")
    void testUpdateExpense_ExceptionHandling() {
        when(calculationService.findById(anyLong())).thenReturn(Optional.of(testExpense));
        doThrow(new RuntimeException()).when(calculationService).updateExpense(testExpense);

        ResponseEntity<?> entity = calculationController.updateExpense(testExpense, anyLong());

        assertEquals(500, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("An error occurred"));

        verify(calculationService,times(1)).findById(anyLong());
        verify(calculationService,times(1)).updateExpense(testExpense);
    }

    /* TEST CASES REGARDING DELETING EXPENSES */
    @Test
    @DisplayName("🗑️ Delete Expense - Should delete expense successfully by ID")
    void testDeleteExpenseById_Success(){
        when(calculationService.findById(anyLong())).thenReturn(Optional.of(testExpense));
        doNothing().when(calculationService).deleteExpenseById(testExpense.getId());

        ResponseEntity<?> entity = calculationController.deleteExpenseById(testExpense.getId());

        assertEquals(204,entity.getStatusCodeValue());
        verify(calculationService,times(1)).findById(testExpense.getId());
        verify(calculationService,times(1)).deleteExpenseById(testExpense.getId());
    }

    @Test
    @DisplayName("🚫 Delete Expense - Should return error when expense not found")
    void testDeleteExpenseById_NotFound(){
        when(calculationService.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<?> entity = calculationController.deleteExpenseById(anyLong());

        assertEquals(500,entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("Expense not found"));

        verify(calculationService, times(1)).findById(anyLong());
        verify(calculationService,times(0)).deleteExpenseById(anyLong());
    }

    @Test
    @DisplayName("💥 Delete Expense - Should handle exception during deletion")
    void testDeleteExpenseById_ExceptionHandling(){
        when(calculationService.findById(anyLong())).thenReturn(Optional.of(testExpense));
        doThrow(new RuntimeException()).when(calculationService).deleteExpenseById(anyLong());

        ResponseEntity<?> entity = calculationController.deleteExpenseById(anyLong());

        assertEquals(500, entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("An error occurred"));

        verify(calculationService, times(1)).findById(anyLong());
        verify(calculationService, times(1)).deleteExpenseById(anyLong());
    }

    /* TEST CASES REGARDING FETCHING CATEGORY / PAYMENT MODE / DATE RANGE EXPENSES */
    @Test
    @DisplayName("📂 Get Expenses by Category - Should return results successfully")
    void testGetExpensesByCategory_Success(){
        when(calculationService.getExpensesByCategory(testExpense.getCategory())).thenReturn(Arrays.asList(testExpense));

        ResponseEntity<?> entity = calculationController.getExpensesByCategory(testExpense.getCategory());

        assertEquals(200,entity.getStatusCodeValue());
        verify(calculationService,times(1)).getExpensesByCategory(testExpense.getCategory());
    }

    @Test
    @DisplayName("📂 Get Expenses by Category - Should return 204 when empty")
    void testGetExpensesByCategory_Empty(){
        when(calculationService.getExpensesByCategory(testExpense.getCategory())).thenReturn(Collections.emptyList());

        ResponseEntity<?> entity = calculationController.getExpensesByCategory(testExpense.getCategory());

        assertEquals(204,entity.getStatusCodeValue());
        verify(calculationService,times(1)).getExpensesByCategory(testExpense.getCategory());
    }

    @Test
    @DisplayName("💳 Get Expenses by Payment Mode - Should return results successfully")
    void testGetExpensesByPaymentMode_Success(){
        when(calculationService.getExpensesByPaymentMode(testExpense.getPaymentMode())).thenReturn(Arrays.asList(testExpense));

        ResponseEntity<?> entity = calculationController.getExpensesByPaymentMode(testExpense.getPaymentMode());

        assertEquals(200,entity.getStatusCodeValue());
        verify(calculationService,times(1)).getExpensesByPaymentMode(testExpense.getPaymentMode());
    }

    @Test
    @DisplayName("💳 Get Expenses by Payment Mode - Should return 204 when empty")
    void testGetExpensesByPaymentMode_Empty(){
        when(calculationService.getExpensesByPaymentMode(testExpense.getPaymentMode())).thenReturn(Collections.emptyList());

        ResponseEntity<?> entity = calculationController.getExpensesByPaymentMode(testExpense.getPaymentMode());

        assertEquals(204,entity.getStatusCodeValue());
        verify(calculationService,times(1)).getExpensesByPaymentMode(testExpense.getPaymentMode());
    }

    @Test
    @DisplayName("📅 Get Expenses by Date Range - Should return results successfully")
    void testGetExpensesByDateRange_Success() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        when(calculationService.getExpensesByDateRange(startDate,endDate))
                .thenReturn(Arrays.asList(testExpense));

        ResponseEntity<?> entity = calculationController.getExpensesByDateRange(startDate,endDate);

        assertEquals(200,entity.getStatusCodeValue());
        verify(calculationService,times(1)).getExpensesByDateRange(startDate,endDate);
    }

    @Test
    @DisplayName("📅 Get Expenses by Date Range - Should return 204 when empty")
    void testGetExpensesByDateRange_Empty(){
        when(calculationService.getExpensesByDateRange(any(),any())).thenReturn(Collections.emptyList());

        ResponseEntity<?> entity = calculationController.getExpensesByDateRange(any(),any());

        assertEquals(204,entity.getStatusCodeValue());
        verify(calculationService,times(1)).getExpensesByDateRange(any(),any());
    }

    @Test
    @DisplayName("📈 Get Total Expenses - Should return all expenses successfully")
    void testGetTotalExpenses_Success(){
        when(calculationService.getTotalExpenses(LocalDate.now())).thenReturn(Arrays.asList(testExpense));

        ResponseEntity<?> entity = calculationController.getTotalExpenses();

        assertEquals(200,entity.getStatusCodeValue());
        verify(calculationService,times(1)).getTotalExpenses(LocalDate.now());
    }

    @Test
    @DisplayName("📈 Get Total Expenses - Should return 204 when no data")
    void testGetTotalExpenses_Empty(){
        when(calculationService.getTotalExpenses(LocalDate.now())).thenReturn(Collections.emptyList());

        ResponseEntity<?> entity = calculationController.getTotalExpenses();

        assertEquals(204,entity.getStatusCodeValue());
        verify(calculationService,times(1)).getTotalExpenses(any());
    }

    @Test
    @DisplayName("🕓 Get Recent Expenses - Should return latest expenses successfully")
    void testGetRecentExpenses_Success(){
        when(calculationService.getRecentExpenses(LocalDate.now())).thenReturn(Arrays.asList(testExpense));

        ResponseEntity<?> entity = calculationController.getRecentExpenses();

        assertEquals(200,entity.getStatusCodeValue());
        verify(calculationService,times(1)).getRecentExpenses(any());
    }

    @Test
    @DisplayName("♻️ Get Recurring Expenses - Should return recurring expenses successfully")
    void testGetRecurringExpenses_Success() {
        Session mockSession = mock(Session.class);
        Query<Expense> mockQuery = mock(Query.class);

        when(sessionFactory.getCurrentSession()).thenReturn(mockSession);
        when(mockSession.createQuery("FROM Expense E WHERE E.isRecurring = true", Expense.class))
                .thenReturn(mockQuery);

        when(mockQuery.list()).thenReturn(Arrays.asList(testExpense));

        ResponseEntity<?> entity = calculationController.getRecurringExpenses();

        assertEquals(200, entity.getStatusCodeValue());
        verify(mockQuery, times(1)).list();
    }

    @Test
    @DisplayName("💸 Get Expenses Above Amount - Should return matching expenses")
    void testGetExpensesAbove_Success(){
        Session mockSession = mock(Session.class);
        Query<Expense> mockQuery = mock(Query.class);

        when(sessionFactory.getCurrentSession()).thenReturn(mockSession);
        when(mockSession.createQuery("FROM Expense E WHERE E.amount > :myAmount",Expense.class)).thenReturn(mockQuery);

        when(mockQuery.setParameter(eq("myAmount"), any(BigDecimal.class))).thenReturn(mockQuery);
        when(mockQuery.list()).thenReturn(Arrays.asList(testExpense));

        ResponseEntity<?> entity = calculationController.getExpensesAbove(new BigDecimal("500.00"));

        assertEquals(200,entity.getStatusCodeValue());
        verify(mockQuery, times(1)).setParameter(eq("myAmount"), any(BigDecimal.class));
        verify(mockQuery, times(1)).list();
    }

    @Test
    @DisplayName("💸 Get Expenses Below Amount - Should return matching expenses")
    void testGetExpensesBelow_Success(){
        Session mockSession = mock(Session.class);
        Query<Expense> mockQuery = mock(Query.class);

        when(sessionFactory.getCurrentSession()).thenReturn(mockSession);
        when(mockSession.createQuery("FROM Expense E WHERE E.amount < :myAmount",Expense.class)).thenReturn(mockQuery);

        when(mockQuery.setParameter(eq("myAmount"), any(BigDecimal.class))).thenReturn(mockQuery);
        when(mockQuery.list()).thenReturn(Arrays.asList(testExpense));

        ResponseEntity<?> entity = calculationController.getExpensesBelow(new BigDecimal("500.00"));

        assertEquals(200,entity.getStatusCodeValue());
        verify(mockQuery, times(1)).setParameter(eq("myAmount"), any(BigDecimal.class));
        verify(mockQuery, times(1)).list();
    }

    @Test
    @DisplayName("📊 Get Expense Summary - Should return calculated summary successfully")
    void testGetExpenseSummary_Success(){
        Map<String, Object> mockSummary = new HashMap<>();
        when(calculationService.getExpenseSummary()).thenReturn(mockSummary);

        ResponseEntity<?> entity = calculationController.getExpenseSummary();

        assertEquals(200,entity.getStatusCodeValue());
        verify(calculationService,times(1)).getExpenseSummary();
    }

    @Test
    @DisplayName("🗓️ Update Expense Date - Should update 'expenseDone' successfully")
    void testUpdateExpenseDone_Success(){
        when(calculationService.findById(anyLong())).thenReturn(Optional.of(testExpense));
        testExpense.setExpenseDone(LocalDate.now());
        doNothing().when(calculationService).updateExpense(testExpense);

        ResponseEntity<?> entity = calculationController.updateExpenseDone(anyLong(),LocalDate.now());

        assertEquals(200,entity.getStatusCodeValue());
        verify(calculationService,times(1)).findById(anyLong());
        verify(calculationService,times(1)).updateExpense(testExpense);
    }

    @Test
    @DisplayName("🚫 Update Expense Date - Should return 500 when expense not found")
    void testUpdateExpenseDone_NotFound(){
        when(calculationService.findById(anyLong())).thenThrow(new RuntimeException());
        ResponseEntity<?> entity = calculationController.updateExpenseDone(anyLong(), LocalDate.now());

        assertEquals(500,entity.getStatusCodeValue());
        assertTrue(entity.getBody().toString().contains("An error occurred"));
        verify(calculationService,times(1)).findById(anyLong());
        verify(calculationService,times(0)).updateExpense(testExpense);
    }

    @Test
    @DisplayName("🔁 Scheduled Task - Should create recurring entries successfully")
    void testCreateRecurringEntries_WithExpenses() {
        when(calculationService.getRecurringExpenses()).thenReturn(Arrays.asList(testExpense));
        doNothing().when(calculationService).addExpense(testExpense);
        doNothing().when(calculationService).updateExpense(testExpense);

        calculationController.createRecurringEntries();

        verify(calculationService, times(1)).getRecurringExpenses();
        verify(calculationService, times(1)).addExpense(testExpense);
        verify(calculationService, times(1)).updateExpense(testExpense);
    }

    @Test
    @DisplayName("🚫 Scheduled Task - Should skip when no recurring expenses found")
    void testCreateRecurringEntries_NoExpenses() {
        when(calculationService.getRecurringExpenses()).thenReturn(Collections.emptyList());
        doNothing().when(calculationService).addExpense(testExpense);
        doNothing().when(calculationService).updateExpense(testExpense);

        calculationController.createRecurringEntries();

        verify(calculationService, times(1)).getRecurringExpenses();
        verify(calculationService, times(0)).addExpense(testExpense);
        verify(calculationService, times(0)).updateExpense(testExpense);
    }

    @Test
    @DisplayName("💥 Scheduled Task - Should handle exception during recurring creation")
    void testCreateRecurringEntries_Exception() {
        when(calculationService.getRecurringExpenses()).thenThrow(new RuntimeException());
        doNothing().when(calculationService).addExpense(testExpense);
        doNothing().when(calculationService).updateExpense(testExpense);

        calculationController.createRecurringEntries();

        verify(calculationService, times(1)).getRecurringExpenses();
        verify(calculationService, times(0)).addExpense(testExpense);
        verify(calculationService, times(0)).updateExpense(testExpense);
    }

}

