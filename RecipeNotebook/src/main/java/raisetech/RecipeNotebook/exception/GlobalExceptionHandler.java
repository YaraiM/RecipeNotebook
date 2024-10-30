package raisetech.RecipeNotebook.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * リクエストパラメータに有効ではない入力形式で入力した場合にエラーメッセージを返すメソッドです。（バリデーション）
   *
   * @param ex 例外クラス（有効ではない入力形式）
   * @return 入力違反が発生したフィールドとエラーメッセージ
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {

    List<Map<String, String>> errors = new ArrayList<>();

    ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
      Map<String, String> error = new HashMap<>();
      error.put("field", fieldError.getField());
      error.put("message", fieldError.getDefaultMessage());
      errors.add(error);
    });

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST,
        "バリデーションエラーです。",
        errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(InvalidJsonFormatException.class)
  public ResponseEntity<ErrorResponse> handleInvalidJsonFormatException(
      InvalidJsonFormatException ex) {
    List<Map<String, String>> errors = new ArrayList<>();
    Map<String, String> error = new HashMap<>();
    error.put("field", "recipeDetail");
    error.put("message", "JSONの形式が不正です: " + ex.getMessage());
    errors.add(error);

    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST,
        "JSONパースエラーです。",
        errors
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

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

  /**
   * レシピIDの不整合などが生じた場合に例外処理を行うメソッドです。
   * RecipeIdMismatchExceptionがスローされたとき、ステータス（NotFound）および指定した例外メッセージを返します。
   * @param e 例外クラス（レシピIDの不整合）
   * @return エラーレスポンス（ステータスおよびメッセージ）
   */
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleRecipeIdMismatchException(
      RecipeIdMismatchException e) {

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleFileStorageException(
      FileStorageException e) {

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
        e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

}
