package raisetech.RecipeNotebook.config;

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
              response.sendRedirect("/recipes");
            })
            .failureUrl("/login?error")
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

}
