package raisetech.RecipeNotebook.exception;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ErrorResponse {

  private final HttpStatus status;
  private final String message;
  private List<Map<String, String>> errors;

  public ErrorResponse(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

}
