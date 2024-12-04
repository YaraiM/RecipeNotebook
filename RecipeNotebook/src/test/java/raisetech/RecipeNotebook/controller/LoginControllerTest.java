package raisetech.RecipeNotebook.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import raisetech.RecipeNotebook.config.SecurityConfig;
import raisetech.RecipeNotebook.repository.UserRepository;

@WebMvcTest(LoginController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "guest.username=testGuest",
    "guest.password=testPass"
})
class LoginControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  AuthenticationManager authenticationManager;

  @Test
  void 処理成功のレスポンスとログイン成功のメッセージが返ること() throws Exception {
    Authentication successAuth = new UsernamePasswordAuthenticationToken(
        "testGuest", "testPass", Collections.emptyList());

    when(authenticationManager.authenticate(any())).thenReturn(successAuth);

    mockMvc.perform(post("/api/login/guest")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("ログイン成功"));

    verify(authenticationManager, times(1)).authenticate(
        any(UsernamePasswordAuthenticationToken.class));

  }

  @Test
  @WithMockUser(username = "user", roles = "USER")
  void ゲストログインが失敗した場合は401とエラーメッセージを返すこと() throws Exception {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("認証失敗"));

    mockMvc.perform(post("/api/login/guest")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            jsonPath("$.message").value("ゲストログインに失敗しました。もう一度お試しください"));

    verify(authenticationManager, times(1))
        .authenticate(any(UsernamePasswordAuthenticationToken.class));
  }
}
