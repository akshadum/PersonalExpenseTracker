package com.application.expenseTracker.ExpenseTracker.Controller;

import com.application.expenseTracker.ExpenseTracker.Entity.Expense;
import com.application.expenseTracker.ExpenseTracker.Exception.ExpenseNotFoundException;
import com.application.expenseTracker.ExpenseTracker.Exception.InvalidBudgetException;
import com.application.expenseTracker.ExpenseTracker.Service.CalculationService;

import com.application.expenseTracker.ExpenseTracker.Service.EmailService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;



@RestController
@RequestMapping("/api/v1/")
public class CalculationController {

    private static final Logger logger = LoggerFactory.getLogger(CalculationController.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/expenses")
    @ExceptionHandler(InvalidBudgetException.class)
    public ResponseEntity<?> addExpense(@RequestBody Expense expense) {
        try {
            logger.debug("Entered addExpense");
            if (expense.getUserEmail() == null || expense.getUserEmail().isEmpty()) {
                logger.warn("Entered userEmail is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please share the Email-Id!");
            } else if (expense.getBudget()<= 0) {
                logger.warn("Entered budget is invalid");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please share a valid Budget!");
            }else{
                calculationService.addExpense(expense);
                logger.info("Expense added successfully");
                calculationService.checkAndSendBudgetAlerts(expense.getUserEmail(),expense.getBudget(),LocalDate.now());
                logger.info("If any budget breached is alerted");
                return ResponseEntity.status(HttpStatus.OK).body(expense);
            }
        } catch (Exception e) {
            logger.error("Error while adding expense: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while adding the expense!" + e.getMessage());
        }

    }

    @GetMapping("/expenses")
    public ResponseEntity<?> getAllExpense() {
        List<Expense> expenses = null;
        try {
            logger.debug("Entered getAllExpense");
            expenses = calculationService.getAllExpense();

            if (expenses == null || expenses.isEmpty()) {
                logger.warn("There are not yet any expenses shared");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            logger.info("Fetched expenses of size: " + expenses.size());
        } catch (Exception e) {
            logger.error("Error while getting all expenses: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching the expenses!" + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(expenses);
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<?> updateExpense(@RequestBody Expense expense, @PathVariable Long id) {
        Optional<Expense> myExpenses = null;
        try {
            calculationService.findById(id).orElseThrow(() -> new ExpenseNotFoundException("Expense not found for ID: " + id));
            calculationService.updateExpense(expense);
            return ResponseEntity.status(HttpStatus.OK).body(myExpenses);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the expenses for id: " + id + " the error is: " + e.getMessage());
        }
    }

    @GetMapping("list/category/{category}")
    public ResponseEntity<?> getExpensesByCategory(@PathVariable Expense.ExpenseCategory category) {
        List<Expense> myExpenses = null;
        try {
            myExpenses = calculationService.getExpensesByCategory(category);
            if (myExpenses.isEmpty())
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching the expenses filtered by category: " + category + " the error is: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(myExpenses);
    }

    @GetMapping("/expenses/date-range")
    public ResponseEntity<?> getExpensesByDateRange(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<Expense> myExpenses = null;
        try {
            myExpenses = calculationService.getExpensesByDateRange(startDate, endDate);
            if (myExpenses.isEmpty())
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching the expenses by date range shared between startDate: " + startDate + " endDate: " + endDate + " the error is: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(myExpenses);
    }

    @DeleteMapping("/expense/{id}")
    public ResponseEntity<?> deleteExpenseById(@PathVariable Long id) {
        try {
            calculationService.findById(id).orElseThrow(() -> new ExpenseNotFoundException("Expense not found for ID: " + id));
            calculationService.deleteExpenseById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the expenses for id: " + id + " the error is: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/expense/delete")
    public ResponseEntity<?> deleteAllExpenses() {
        try {
            calculationService.deleteAllExpenses();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the expenses the error is: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/expenses/payment/{paymentMode}")
    public ResponseEntity<?> getExpensesByPaymentMode(@PathVariable Expense.PaymentMode paymentMode) {
        List<Expense> myExpenses = null;
        try {
            myExpenses = calculationService.getExpensesByPaymentMode(paymentMode);
            if (myExpenses.isEmpty())
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching the expenses filtered by category: " + paymentMode + " the error is: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(myExpenses);
    }


    @GetMapping("/expenses/total/")
    public ResponseEntity<?> getTotalExpenses() {
        List<Expense> myExpenses = null;
        try {
            myExpenses = calculationService.getTotalExpenses(LocalDate.now());
            if (myExpenses.isEmpty())
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching all the expenses the error is: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(myExpenses);
    }

    @GetMapping("/expenses/recent/")
    public ResponseEntity<?> getRecentExpenses() {
        List<Expense> myExpenses = null;
        try {
            myExpenses = calculationService.getRecentExpenses(LocalDate.now());
            if (myExpenses.isEmpty())
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching all the expenses the error is: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(myExpenses);
    }

    @Transactional
    @GetMapping("/expenses/recurring/")
    public ResponseEntity<?> getRecurringExpenses() {
        List<Expense> myExpenses = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            String hql = "FROM Expense E WHERE E.isRecurring = true";
            myExpenses = session.createQuery(hql, Expense.class).list();
            if (myExpenses.isEmpty())
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching all the expenses the error is: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(myExpenses);
    }

    @Transactional
    @GetMapping("/expenses/amount-above/{myAmount}/")
    public ResponseEntity<?> getExpensesAbove(@PathVariable BigDecimal myAmount) {
        List<Expense> myExpenses = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            String hql = "FROM Expense E WHERE E.amount > :myAmount";
            myExpenses = session.createQuery(hql, Expense.class).setParameter("myAmount", myAmount).list();
            if (myExpenses.isEmpty())
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching all the expenses the error is: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(myExpenses);
    }

    @Transactional
    @GetMapping("/expenses/amount-below/{myAmount}/")
    public ResponseEntity<?> getExpensesBelow(@PathVariable BigDecimal myAmount) {
        List<Expense> myExpenses = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            String hql = "FROM Expense E WHERE E.amount < :myAmount";
            myExpenses = session.createQuery(hql, Expense.class).setParameter("myAmount", myAmount).list();
            if (myExpenses.isEmpty())
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching all the expenses the error is: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(myExpenses);
    }

    @GetMapping("/expenses/summary")
    public ResponseEntity<?> getExpenseSummary() {
        try{
            Map<String, Object> summary = calculationService.getExpenseSummary();
            return ResponseEntity.status(HttpStatus.OK).body(summary);
        }
        catch(Exception e){
            logger.error("An error occurred while getting ExpenseSummary!: {} ", e.getMessage(), e );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching all the expenses the error is: " + e.getMessage());
        }
    }

    @PostMapping("/expenses/{myid}/newdate")
    public ResponseEntity<?> updateExpenseDone(@PathVariable Long myid, @RequestParam LocalDate newdate){
        try{
            logger.debug("Entered updateExpenseDone");
            Expense expense = calculationService.findById(myid).orElseThrow(() -> new ExpenseNotFoundException("Expense not found for ID: " + myid));
            expense.setExpenseDone(newdate);
            calculationService.updateExpense(expense);
            logger.debug("Updated ExpenseDone for id: " + myid +" with newdate: "+ newdate);
            return ResponseEntity.status(HttpStatus.OK).body(expense);
        }catch(Exception e) {
            logger.error("An error occurred while getting ExpenseSummary!: {} ", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching all the expenses the error is: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void createRecurringEntries(){
        try {
            logger.debug("Entered Scheduled Job to create Recurring Expenses");
            List<Expense> expenses = calculationService.getRecurringExpenses();
            if (expenses.isEmpty()) {
                logger.warn("No Recurring Expense present");
            }else{
                for(Expense expense: expenses) {
                    logger.debug("Adding Expense with Title: " + expense.getTitle() + " and Amount: " + expense.getAmount() + " for Category: " + expense.getCategory());
                    calculationService.addExpense(expense);
                    expense.setExpenseDone(LocalDate.now());
                    calculationService.updateExpense(expense);
                }
            }
        }catch (Exception e){
            logger.error("An error occurred while creating/fetching the recurring expenses!: {} ", e.getMessage(), e );
        }
    }
}
