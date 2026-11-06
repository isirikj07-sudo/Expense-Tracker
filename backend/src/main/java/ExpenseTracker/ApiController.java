package ExpenseTracker;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@RestController @RequestMapping("/api")
public class ApiController {
 private final Users users; private final Entries entries; private final Budgets budgets; private final BCryptPasswordEncoder passwords;
 static final List<String> CATEGORIES=List.of("Food & dining","Shopping","Transport","Housing","Utilities","Health","Entertainment","Education","Travel","Salary","Freelance","Other");
 ApiController(Users u,Entries e,Budgets b,BCryptPasswordEncoder p){users=u;entries=e;budgets=b;passwords=p;}
 record Register(@NotBlank @Size(max=80) String name,@NotBlank @Email @Size(max=254) String email,@NotBlank @Size(min=8,max=72) String password){}
 record Login(@NotBlank String email,@NotBlank @Size(max=72) String password){}
 record EntryInput(@NotBlank @Size(max=120) String description,@NotNull @DecimalMin("0.01") @DecimalMax("999999999999.99") @Digits(integer=12,fraction=2) BigDecimal amount,@Pattern(regexp="INCOME|EXPENSE") @NotNull String type,@NotBlank String category,@NotNull LocalDate date,@Size(max=500) String notes){}
 record BudgetInput(@NotBlank String month,@NotBlank String category,@NotNull @DecimalMin("0.01") @DecimalMax("999999999999.99") @Digits(integer=12,fraction=2) BigDecimal amount){}
 @GetMapping("/csrf") Map<String,String> csrf(CsrfToken token){return Map.of("token",token.getToken());}
 @GetMapping("/categories") List<String> categories(){return CATEGORIES;}
 @PostMapping("/auth/register") synchronized Map<String,Object> register(@Valid @RequestBody Register r,HttpServletRequest request){
  if(r.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw error(400,"Password must be at most 72 UTF-8 bytes.");
  String email=r.email().strip().toLowerCase(Locale.ROOT);
  if(users.findByEmail(email).isPresent()) throw error(409,"An account with this email already exists.");
  Models.User u=new Models.User();u.name=r.name().strip();u.email=email;u.password=passwords.encode(r.password());users.save(u);start(request,u.id);return view(u);
 }
 @PostMapping("/auth/login") Map<String,Object> login(@Valid @RequestBody Login r,HttpServletRequest request){
  Models.User u=users.findByEmail(r.email().strip().toLowerCase(Locale.ROOT)).orElseThrow(()->error(401,"Incorrect email or password."));
  if(!passwords.matches(r.password(),u.password)) throw error(401,"Incorrect email or password.");
  start(request,u.id);return view(u);
 }
 void start(HttpServletRequest r,Long id){if(r.getSession(false)!=null)r.getSession(false).invalidate();r.getSession(true).setAttribute("uid",id);}
 Map<String,Object> view(Models.User u){return Map.of("id",u.id,"name",u.name,"email",u.email);}
 Long uid(HttpServletRequest r){HttpSession s=r.getSession(false);if(s==null||s.getAttribute("uid")==null)throw error(401,"Please sign in.");return (Long)s.getAttribute("uid");}
 @GetMapping("/auth/me") Map<String,Object> me(HttpServletRequest r){return view(users.findById(uid(r)).orElseThrow(()->error(401,"Please sign in.")));}
 @PostMapping("/auth/logout") void logout(HttpServletRequest r){if(r.getSession(false)!=null)r.getSession(false).invalidate();}
 YearMonth month(String m){try{return YearMonth.parse(m);}catch(Exception e){throw error(400,"Use a month in YYYY-MM format.");}}
 void category(String c){if(!CATEGORIES.contains(c))throw error(400,"Choose a valid category.");}
 @GetMapping("/transactions") List<Models.Entry> list(@RequestParam String month,HttpServletRequest r){YearMonth m=month(month);return entries.findByUserIdAndDateBetweenOrderByDateDescIdDesc(uid(r),m.atDay(1),m.atEndOfMonth());}
 @PostMapping("/transactions") Models.Entry create(@Valid @RequestBody EntryInput input,HttpServletRequest r){Models.Entry e=new Models.Entry();e.userId=uid(r);return save(e,input);}
 @PutMapping("/transactions/{id}") Models.Entry update(@PathVariable Long id,@Valid @RequestBody EntryInput input,HttpServletRequest r){return save(entries.findByIdAndUserId(id,uid(r)).orElseThrow(()->error(404,"Transaction not found.")),input);}
 Models.Entry save(Models.Entry e,EntryInput i){category(i.category());e.description=i.description().strip();e.amount=i.amount();e.type=i.type();e.category=i.category();e.date=i.date();e.notes=i.notes();return entries.save(e);}
 @DeleteMapping("/transactions/{id}") void delete(@PathVariable Long id,HttpServletRequest r){entries.delete(entries.findByIdAndUserId(id,uid(r)).orElseThrow(()->error(404,"Transaction not found.")));}
 @GetMapping("/budgets") List<Models.Budget> budgets(@RequestParam String month,HttpServletRequest r){month(month);return budgets.findByUserIdAndMonth(uid(r),month);}
 @PostMapping("/budgets") synchronized Models.Budget budget(@Valid @RequestBody BudgetInput i,HttpServletRequest r){month(i.month());category(i.category());Long id=uid(r);Models.Budget b=budgets.findByUserIdAndMonthAndCategory(id,i.month(),i.category()).orElseGet(Models.Budget::new);b.userId=id;b.month=i.month();b.category=i.category();b.amount=i.amount();return budgets.save(b);}
 @DeleteMapping("/budgets/{id}") void deleteBudget(@PathVariable Long id,HttpServletRequest r){budgets.delete(budgets.findByIdAndUserId(id,uid(r)).orElseThrow(()->error(404,"Budget not found.")));}
 static ResponseStatusException error(int code,String message){return new ResponseStatusException(HttpStatusCode.valueOf(code),message);}
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<?> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",Objects.requireNonNullElse(e.getReason(),"Request failed.")));}
 @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class) ResponseEntity<?> validation(org.springframework.web.bind.MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("message",e.getBindingResult().getFieldErrors().stream().map(f->f.getField()+": "+f.getDefaultMessage()).findFirst().orElse("Invalid input.")));}
 @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class}) ResponseEntity<?> malformed(Exception e){return ResponseEntity.badRequest().body(Map.of("message","Check the request values and try again."));}
}
