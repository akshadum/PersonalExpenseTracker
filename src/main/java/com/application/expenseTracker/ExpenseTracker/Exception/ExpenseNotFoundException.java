package com.application.expenseTracker.ExpenseTracker.Exception;

public class ExpenseNotFoundException extends RuntimeException {
    public ExpenseNotFoundException(String msg){
        super(msg);
    }
}
