package com.application.expenseTracker.ExpenseTracker.Exception;

public class InvalidBudgetException extends RuntimeException {
    public InvalidBudgetException(String msg){
        super(msg);
    }
}
