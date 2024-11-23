package raisetech.RecipeNotebook.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

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