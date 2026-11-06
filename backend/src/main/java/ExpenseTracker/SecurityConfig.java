package ExpenseTracker;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
@Configuration
public class SecurityConfig {
 @Bean org.springframework.security.core.userdetails.UserDetailsService userDetailsService(){return username->{throw new org.springframework.security.core.userdetails.UsernameNotFoundException(username);};}
 @Bean BCryptPasswordEncoder passwords(){return new BCryptPasswordEncoder();}
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
  return http.authorizeHttpRequests(a->a.anyRequest().permitAll())
   .csrf(c->c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
   .headers(h->h.contentSecurityPolicy(c->c.policyDirectives("default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self'; img-src 'self' data:; frame-ancestors 'none'")))
   .build();
 }
}
