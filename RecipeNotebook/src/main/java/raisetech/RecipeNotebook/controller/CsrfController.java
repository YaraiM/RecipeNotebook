package raisetech.RecipeNotebook.controller;

import io.swagger.v3.oas.annotations.Operation;
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
  public CsrfToken csrf(CsrfToken csrfToken) {
    return csrfToken;
  }

}
