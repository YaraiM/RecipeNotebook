package raisetech.RecipeNotebook.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.ignoringRequestMatchers("/csrf-token"))
        .formLogin(login -> login
            .loginProcessingUrl("/login")
            .loginPage("/login")
            .successHandler((request, response, authentication) -> {
              String targetUrl = getRedirectUrlFromSavedRequest(request);
              response.sendRedirect(targetUrl);
            })
            .failureHandler((request, response, exception) -> {
              new HttpSessionRequestCache().removeRequest(request, response);
              response.sendRedirect("/login?error");
            })
            .permitAll())
        .logout(logout -> logout
            .logoutSuccessUrl("/login"))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/csrf-token").permitAll()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .requestMatchers("/login").permitAll()
            .anyRequest().authenticated()
        );
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails user = User.builder()
        .username("user")
        .password("{noop}password")
        .roles("USER")
        .build();

    return new InMemoryUserDetailsManager(user);
  }

  private String getRedirectUrlFromSavedRequest(HttpServletRequest request) {
    RequestCache requestCache = new HttpSessionRequestCache();
    SavedRequest savedRequest = requestCache.getRequest(request, null);

    if (savedRequest == null || savedRequest.getRedirectUrl().contains("/error")) {
      return "/recipes";
    }

    return savedRequest.getRedirectUrl();
  }

}
