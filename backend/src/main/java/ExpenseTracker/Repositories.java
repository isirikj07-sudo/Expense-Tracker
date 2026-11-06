package ExpenseTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.LocalDate;
interface Users extends JpaRepository<Models.User,Long> { Optional<Models.User> findByEmail(String email); }
interface Entries extends JpaRepository<Models.Entry,Long> {
 List<Models.Entry> findByUserIdAndDateBetweenOrderByDateDescIdDesc(Long userId,LocalDate start,LocalDate end);
 Optional<Models.Entry> findByIdAndUserId(Long id,Long userId);
}
interface Budgets extends JpaRepository<Models.Budget,Long> {
 List<Models.Budget> findByUserIdAndMonth(Long userId,String month);
 Optional<Models.Budget> findByUserIdAndMonthAndCategory(Long userId,String month,String category);
 Optional<Models.Budget> findByIdAndUserId(Long id,Long userId);
}
