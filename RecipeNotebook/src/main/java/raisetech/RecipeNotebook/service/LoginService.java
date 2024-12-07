package raisetech.RecipeNotebook.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

  private final AuthenticationManager authenticationManager;

  @Value("${guest.username}")
  private String guestUsername;

  @Value("${guest.password}")
  private String guestPassword;

  @Autowired
  public LoginService(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  /**
   * ゲストログイン用の認証情報を生成し認証を行います。
   *
   * @return ゲストログイン用の認証情報
   */
  public Authentication authenticateGuest() {
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(guestUsername, guestPassword);

    return authenticationManager.authenticate(authToken);
  }

  /**
   * ログインした状態を現在のスレッドに保存します。同一リクエスト内の後続の処理で認証状態を参照可能になります。
   *
   * @param authentication ログイン用の認証情報
   */
  public void setAuthenticationInContext(Authentication authentication) {
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  /**
   * 認証情報をHTTPセッションに保存します。ログイン後のリクエストでログイン状態を維持します。
   *
   * @param request httpリクエスト
   */
  public void setAuthenticationInSession(HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        SecurityContextHolder.getContext()
    );
  }

}
