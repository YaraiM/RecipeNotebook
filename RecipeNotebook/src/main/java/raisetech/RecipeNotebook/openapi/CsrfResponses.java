package raisetech.RecipeNotebook.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class CsrfResponses {

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200",
      description = "CSRFトークンの取得処理が成功した場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          examples = {
              @ExampleObject(
                  name = "success(get a csrf token)",
                  summary = "CSRFトークンの取得に成功した場合",
                  description = "parameterName、headerName、tokenの値を返します。",
                  value = """
                      {
                          "parameterName": "_csrf",
                          "token": "token1234",
                          "headerName": "X-CSRF-TOKEN"
                      }
                      """
              )
          }
      )
  )
  public @interface GetCsrfTokenSuccess {

  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @GetCsrfTokenSuccess
  public @interface GetCsrfTokenResponses {

  }

}
