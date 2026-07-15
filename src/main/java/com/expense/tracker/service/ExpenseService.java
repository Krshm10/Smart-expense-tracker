package com.expense.tracker.service;

import com.expense.tracker.model.Expense;
import com.expense.tracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

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
    

    // Filter by month
    public List<Expense> getByMonth(int year, int month) {
        return repo.findAll().stream()
                .filter(e -> e.getDate().getYear() == year && e.getDate().getMonthValue() == month)
                .collect(Collectors.toList());
    }

    // Filter by year
    public List<Expense> getByYear(int year) {
        return repo.findAll().stream()
                .filter(e -> e.getDate().getYear() == year)
                .collect(Collectors.toList());
    }

    // Full analysis
    public Map<String, Object> getAnalysis(int year, Integer month) {
        List<Expense> expenses = (month != null) ? getByMonth(year, month) : getByYear(year);

        Map<String, Object> analysis = new LinkedHashMap<>();

        if (expenses.isEmpty()) {
            analysis.put("message", "No expenses found for this period");
            return analysis;
        }

        // Total spent
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        analysis.put("totalSpent", total);

        // Category breakdown
        Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)));
        analysis.put("categoryBreakdown", categoryTotals);

        // Highest spending category
        String topCategory = categoryTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();
        analysis.put("highestCategory", topCategory);

        // Highest spending day
        Map<String, Double> dayTotals = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getDate().toString(),
                        Collectors.summingDouble(Expense::getAmount)));
        String topDay = dayTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();
        analysis.put("highestSpendingDay", topDay);
        analysis.put("highestSpendingDayAmount", dayTotals.get(topDay));

        // Monthly totals (for yearly view)
        if (month == null) {
            Map<String, Double> monthlyTotals = new LinkedHashMap<>();
            String[] monthNames = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
            for (int m = 1; m <= 12; m++) {
                final int fm = m;
                double monthTotal = expenses.stream()
                        .filter(e -> e.getDate().getMonthValue() == fm)
                        .mapToDouble(Expense::getAmount).sum();
                if (monthTotal > 0)
                    monthlyTotals.put(monthNames[m - 1], monthTotal);
            }
            analysis.put("monthlyTotals", monthlyTotals);
        }

        // Compare to last month
        if (month != null) {
            int lastMonth = month == 1 ? 12 : month - 1;
            int lastYear = month == 1 ? year - 1 : year;
            List<Expense> lastMonthExpenses = getByMonth(lastYear, lastMonth);
            double lastMonthTotal = lastMonthExpenses.stream()
                    .mapToDouble(Expense::getAmount).sum();
            double difference = total - lastMonthTotal;
            analysis.put("lastMonthTotal", lastMonthTotal);
            analysis.put("comparedToLastMonth",
                    difference > 0 ? "+" + String.format("%.0f", difference) + " more than last month"
                            : String.format("%.0f", Math.abs(difference)) + " less than last month");
        }

        analysis.put("totalTransactions", expenses.size());
        analysis.put("averagePerTransaction", total / expenses.size());

        return analysis;
    }

    public String getAiInsights() {
        List<Expense> all = repo.findAll();
        if (all.isEmpty())
            return "Add some expenses first!";
        return aiInsightService.getInsights(all);
    }
    
    public Expense updateExpense(Long id, Expense updatedExpense) {
        Expense existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        existing.setAmount(updatedExpense.getAmount());
        existing.setCategory(updatedExpense.getCategory());
        existing.setDate(updatedExpense.getDate());
        existing.setDescription(updatedExpense.getDescription());

        return repo.save(existing);
    }
}