package raisetech.RecipeNotebook.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import raisetech.RecipeNotebook.openapi.CsrfResponses.GetCsrfTokenResponses;

@RestController
public class CsrfController {

  @Operation(
      summary = "CSRFトークンの取得",
      description = "SpringSecurityの機能でCSRFトークンを取得します。")
  @GetCsrfTokenResponses
  @GetMapping("/csrf-token")
  public Map<String, String> getCsrfToken(HttpServletRequest request) {
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    if (csrfToken != null) {
      return Map.of(
          "token", csrfToken.getToken(),
          "headerName", csrfToken.getHeaderName()
      );
    }
    throw new IllegalStateException("CSRFトークンが取得できませんでした");
  }

}
