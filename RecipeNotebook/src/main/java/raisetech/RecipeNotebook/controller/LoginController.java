package raisetech.RecipeNotebook.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginController {

  private final AuthenticationManager authenticationManager;

  @Value("${guest.username}")
  private String guestUsername;

  @Value("${guest.password}")
  private String guestPassword;

  public LoginController(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/login/guest")
  public ResponseEntity<?> guestLogin(HttpServletRequest request) {
    try {
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(guestUsername, guestPassword);

      Authentication authentication = authenticationManager.authenticate(authToken);

      // ログイン処理での認証情報を保存（リクエスト完了後に破棄）
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // セッションに認証情報を保存（次回以降のリクエストに使用）
      HttpSession session = request.getSession(true);
      session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
          SecurityContextHolder.getContext());

      Map<String, String> response = new HashMap<>();
      response.put("message", "ログイン成功");

      return ResponseEntity.ok(response);

    } catch (AuthenticationException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("message", "ゲストログインに失敗しました。もう一度お試しください");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(errorResponse);
    }
  }

}
