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
   * 認証処理が失敗した場合の例外をハンドリングするメソッドです。
   *
   * @param e 例外クラス（認証失敗）
   * @return エラーレスポンス（ステータスおよびメッセージ）
   */
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleAuthenticationCustomException(
      AuthenticationCustomException e) {

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /**
   * リクエストパラメータに有効ではない入力形式で入力した場合の例外をハンドリングするメソッドです。（バリデーション）
   *
   * @param e 例外クラス（有効ではない入力形式）
   * @return 入力違反が発生したフィールドとエラーメッセージ
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {

    List<Map<String, String>> errors = new ArrayList<>();

    e.getBindingResult().getFieldErrors().forEach(fieldError -> {
      Map<String, String> error = new HashMap<>();
      error.put("field", fieldError.getField());
      error.put("message", fieldError.getDefaultMessage());
      errors.add(error);
    });

    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST, "バリデーションエラーです。入力フォームを確認してください", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * リクエスト内のオブジェクトがNullもしくは空白だった場合の例外をハンドリングするメソッドです。
   * NullOrEmptyExceptionがスローされたとき、ステータス（BadRequest）および指定した例外メッセージを返します。
   * @param e 例外クラス（オブジェクトが入力されていない）
   * @return エラーレスポンス（ステータスおよびメッセージ）
   */
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleNullOrEmptyObjectException(
      NullOrEmptyObjectException e) {

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * 存在しないIDなどを指定した場合に例外をハンドリングするメソッドです。
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
   * レシピIDの不整合などが生じた場合に例外をハンドリングするメソッドです。
   * RecipeIdMismatchExceptionがスローされたとき、ステータス（BadRequest）および指定した例外メッセージを返します。
   * @param e 例外クラス（レシピIDの不整合）
   * @return エラーレスポンス（ステータスおよびメッセージ）
   */
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleRecipeIdMismatchException(
      RecipeIdMismatchException e) {

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * 不正な形式のデータを入力した場合などに発生する例外をハンドリングするメソッドです。
   * 既存クラスのIllegalArgumentExceptionがスローされたとき、ステータス（BadRequest）および指定した例外メッセージを返します。
   *
   * @param e 例外クラス（不正なファイル形式を指定）
   * @return エラーレスポンス（ステータスおよびメッセージ）
   */
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleIllegalArgumentCustomException(
      IllegalArgumentCustomException e) {

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST,
        e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * 5MBを超える画像をアップロードしようとした場合の例外をハンドリングするメソッドです。
   * 既存クラスのFileSizeLimitExceededExceptionがスローされたとき、ステータス（BadRequest）および指定した例外メッセージを返します。
   *
   * @param e 例外クラス（画像以外のファイル形式を指定）
   * @return エラーレスポンス（ステータスおよびメッセージ）
   */
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleFileSizeLimitExceededCustomException(
      FileSizeLimitExceededCustomException e) {

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE,
        e.getMessage());
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
  }

  /**
   * 画像以外のファイル形式をアップロードしようとした場合の例外をハンドリングするメソッドです。
   * InvalidFileTypeExceptionがスローされたとき、ステータス（BadRequest）および指定した例外メッセージを返します。
   *
   * @param e 例外クラス（画像以外のファイル形式を指定）
   * @return エラーレスポンス（ステータスおよびメッセージ）
   */
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleInvalidFileTypeException(
      InvalidFileTypeException e) {

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST,
        e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * 何らかの理由で画像のアップロードに失敗した場合に例外をハンドリングするメソッドです。
   * FileStorageExceptionがスローされたとき、ステータス（INTERNAL_SERVER_ERROR）および指定した例外メッセージを返します。
   *
   * @param e 例外クラス（画像のアップロードに失敗）
   * @return エラーレスポンス（ステータスおよびメッセージ）
   */
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleFileStorageException(
      FileStorageException e) {

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
        e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

}
