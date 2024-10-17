package raisetech.RecipeNotebook.exception;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;

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
