package com.expense.tracker.service;

import com.expense.tracker.model.Expense;
import com.expense.tracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository repo;

    @Autowired
    private AiInsightService aiInsightService;

    public Expense addExpense(Expense expense) {
        return repo.save(expense);
    }

    public List<Expense> getAllExpenses() {
        return repo.findAll();
    }

    public void deleteExpense(Long id) {
        repo.deleteById(id);
    }

    public String getAiInsights() {
        List<Expense> all = repo.findAll();
        if (all.isEmpty())
            return "Add some expenses first!";
        return aiInsightService.getInsights(all);
    }
}