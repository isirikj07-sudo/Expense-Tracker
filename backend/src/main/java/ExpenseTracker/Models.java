package ExpenseTracker;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Models {
 @Entity @Table(name="app_users")
 public static class User {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(nullable=false,unique=true,length=254) public String email;
  @Column(nullable=false) public String password;
  @Column(nullable=false) public String name;
 }
 @Entity @Table(name="transactions", indexes=@Index(columnList="userId,date"))
 public static class Entry {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(nullable=false) public Long userId;
  @Column(nullable=false,length=120) public String description;
  @Column(nullable=false,precision=14,scale=2) public BigDecimal amount;
  @Column(nullable=false) public String type;
  @Column(nullable=false) public String category;
  @Column(nullable=false) public LocalDate date;
  @Column(length=500) public String notes;
 }
 @Entity @Table(name="budgets",uniqueConstraints=@UniqueConstraint(columnNames={"userId","budget_month","category"}))
 public static class Budget {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(nullable=false) public Long userId;
  @Column(nullable=false,name="budget_month") public String month;
  @Column(nullable=false) public String category;
  @Column(nullable=false,precision=14,scale=2) public BigDecimal amount;
 }
}
