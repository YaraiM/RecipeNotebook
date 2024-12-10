package raisetech.RecipeNotebook.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class LoginResponses {

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "200",
      description = "ゲストログイン処理が成功した場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          examples = {
              @ExampleObject(
                  name = "success(login by guest)",
                  summary = "ゲストログインに成功した場合",
                  description = "メッセージを返します。",
                  value = """
                      {
                          "message": "ログイン成功"
                      }
                      """
              )
          }
      )
  )
  public @interface GuestLoginSuccess {

  }

  @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ApiResponse(
      responseCode = "401",
      description = "ゲストログインの認証に失敗した場合のレスポンスです。",
      content = @Content(
          mediaType = "application/json",
          examples = {
              @ExampleObject(
                  name = "guest login failed",
                  summary = "ゲストログイン認証に失敗した場合",
                  description = "エラーステータスとメッセージを返します。",
                  value = """
                      {
                         "status": "UNAUTHORIZED",
                         "message": "ゲストログインに失敗しました。もう一度お試しください",
                         "errors": null
                      }
                      """
              )
          }
      )
  )
  public @interface GuestLoginFailed {

  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @GuestLoginSuccess
  @GuestLoginFailed
  public @interface GuestLoginResponses {

  }

}
