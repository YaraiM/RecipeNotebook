package raisetech.RecipeNotebook.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Schema(description = "例外発生時のレスポンス")
@Getter
public class ErrorResponse {

  private final HttpStatus status;
  private final String message;
  private List<Map<String, String>> errors;

  public ErrorResponse(HttpStatus status, String message, List<Map<String, String>> errors) {
    this.status = status;
    this.message = message;
    this.errors = errors;
  }

  public ErrorResponse(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

}
