package raisetech.RecipeNotebook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import raisetech.RecipeNotebook.exception.AuthenticationCustomException;
import raisetech.RecipeNotebook.service.LoginService;

@RestController
@RequestMapping("/api")
public class GuestLoginController {

  private final LoginService loginService;

  @Autowired
  public GuestLoginController(LoginService loginService) {
    this.loginService = loginService;
  }

  @Operation(
      summary = "ゲストログインの実行",
      description = "ゲストログインを行います。")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "処理が成功した場合のレスポンスです。",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      summary = "ゲストログインに成功した場合",
                      value = """
                          {
                              "message": "ログイン成功"
                          }
                          """
                  )
              }
          )
      ),
      @ApiResponse(responseCode = "401", description = "ゲストログインに認証に失敗した場合のレスポンスです。",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      summary = "ゲストログイン認証に失敗した場合",
                      value = """
                          {
                              "message": "ゲストログインに失敗しました。もう一度お試しください"
                          }
                          """
                  )
              }
          )
      )
  })
  @PostMapping("/login/guest")
  public ResponseEntity<?> guestLogin(HttpServletRequest request) {
    try {
      Authentication authentication = loginService.authenticateGuest();
      loginService.setAuthenticationInContext(authentication);
      loginService.setAuthenticationInSession(request);

      Map<String, String> response = new HashMap<>();
      response.put("message", "ログイン成功");

      return ResponseEntity.ok(response);

    } catch (AuthenticationException e) {
      throw new AuthenticationCustomException(
          "ゲストログインに失敗しました。もう一度お試しください");
    }
  }

}
