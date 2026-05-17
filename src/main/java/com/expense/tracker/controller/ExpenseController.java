package com.expense.tracker.controller;

import com.expense.tracker.model.Expense;
import com.expense.tracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExpenseController {

    @Autowired
    private ExpenseService service;

    @PostMapping("/expenses")
    public Expense addExpense(@RequestBody Expense expense) {
        return service.addExpense(expense);
    }

    @GetMapping("/expenses")
    public List<Expense> getAllExpenses() {
        return service.getAllExpenses();
    }

    @GetMapping("/expenses/monthly")
    public List<Expense> getMonthlyExpenses(@RequestParam int year, @RequestParam int month) {
        return service.getByMonth(year, month);
    }

    @GetMapping("/expenses/yearly")
    public List<Expense> getYearlyExpenses(@RequestParam int year) {
        return service.getByYear(year);
    }

    @GetMapping("/analysis")
    public Map<String, Object> getAnalysis(@RequestParam int year, @RequestParam(required = false) Integer month) {
        return service.getAnalysis(year, month);
    }

    @DeleteMapping("/expenses/{id}")
    public void deleteExpense(@PathVariable Long id) {
        service.deleteExpense(id);
    }

    @GetMapping("/insights")
    public String getInsights() {
        return service.getAiInsights();
    }
}