package raisetech.RecipeNotebook.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import raisetech.RecipeNotebook.exception.AuthenticationCustomException;
import raisetech.RecipeNotebook.openapi.LoginResponses.GuestLoginResponses;
import raisetech.RecipeNotebook.service.GuestLoginService;

/**
 * ゲストログインを行うためのコントローラーです。
 */
@RestController
public class GuestLoginController {

  private final GuestLoginService guestLoginService;

  @Autowired
  public GuestLoginController(GuestLoginService guestLoginService) {
    this.guestLoginService = guestLoginService;
  }

  @Operation(
      summary = "ゲストログインの実行",
      description = "ゲストログインを行います。"
  )
  @GuestLoginResponses
  @PostMapping("/login/guest")
  public ResponseEntity<?> guestLogin(HttpServletRequest request) {
    try {
      Authentication authentication = guestLoginService.authenticateGuest();
      guestLoginService.setAuthenticationInContext(authentication);
      guestLoginService.setAuthenticationInSession(request);

      Map<String, String> response = new HashMap<>();
      response.put("message", "ログイン成功");

      return ResponseEntity.ok(response);

    } catch (AuthenticationException e) {
      throw new AuthenticationCustomException(
          "ゲストログインに失敗しました。もう一度お試しください");
    }
  }

}
