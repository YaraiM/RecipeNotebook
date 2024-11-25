package raisetech.RecipeNotebook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

  @Operation(
      summary = "CSRFトークンの取得",
      description = "SpringSecurityの機能でCSRFトークンを取得します。")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "処理が成功した場合のレスポンスです。",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      summary = "CSRFトークンの取得に成功した場合",
                      value = """
                          {
                              "token": "token1234",
                              "headerName": "X-CSRF-TOKEN"
                          }
                          """
                  )
              }
          )
      )
  })
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
