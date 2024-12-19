package raisetech.RecipeNotebook.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@ExtendWith(MockitoExtension.class)
class GuestLoginServiceTest {

  @Mock
  private AuthenticationManager authenticationManager;

  @InjectMocks
  private GuestLoginService sut;

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void ゲストログイン用の認証情報を生成および認証するメソッドが実行されること() {
    String testUserName = "testUserName";
    String testUserPassword = "testUserPassword";
    UsernamePasswordAuthenticationToken testToken =
        new UsernamePasswordAuthenticationToken(testUserName, testUserPassword);

    when(authenticationManager.authenticate(
        any(UsernamePasswordAuthenticationToken.class))).thenReturn(testToken);

    Authentication actual = sut.authenticateGuest();

    assertThat(actual.getPrincipal(), is(testUserName));
    assertThat(actual.getCredentials(), is(testUserPassword));
    verify(authenticationManager, times(1)).authenticate(
        any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void 認証情報がスレッドに保存されること() {
    Authentication testAuthentication = mock(Authentication.class);

    sut.setAuthenticationInContext(testAuthentication);

    SecurityContext context = SecurityContextHolder.getContext();
    assertThat(context.getAuthentication(), is(testAuthentication));
  }

  @Test
  void 認証情報をセッションに保存するメソッドが実行されること() {
    HttpServletRequest testRequest = mock(HttpServletRequest.class);
    HttpSession testSession = mock(HttpSession.class);
    SecurityContext testContext = SecurityContextHolder.getContext();
    when(testRequest.getSession(true)).thenReturn(testSession);

    sut.setAuthenticationInSession(testRequest);

    verify(testSession, times(1)).setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        testContext);
  }

}
