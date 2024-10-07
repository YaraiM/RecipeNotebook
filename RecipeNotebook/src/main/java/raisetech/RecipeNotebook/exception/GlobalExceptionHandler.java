package raisetech.RecipeNotebook.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 存在しないIDなどを指定した場合に例外処理を行うメソッドです。
   * ResourceNotFoundExceptionがスローされたとき、ステータス（NotFound）および指定した例外メッセージを返します。
   * @param e 例外クラス（リソースが存在しない）
   * @return エラーレスポンス（ステータスおよびメッセージ）
   */
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException e) {

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

}
