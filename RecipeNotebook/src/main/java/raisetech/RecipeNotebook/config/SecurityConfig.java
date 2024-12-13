package raisetech.RecipeNotebook.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import raisetech.RecipeNotebook.repository.UserRepository;
import raisetech.RecipeNotebook.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.ignoringRequestMatchers("/csrf-token", "/v3/api-docs/**"))
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
        .httpBasic(Customizer.withDefaults()) // SwaggerUI用の認証
        .logout(logout -> logout
            .logoutSuccessUrl("/login"))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/csrf-token", "/v3/api-docs/**").permitAll()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .requestMatchers("/login", "/login/guest").permitAll()
            .anyRequest().authenticated()
        );
    return http.build();
  }

  @Bean
  public CustomUserDetailsService customUserDetailsService(UserRepository userRepository) {
    return new CustomUserDetailsService(userRepository);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
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
