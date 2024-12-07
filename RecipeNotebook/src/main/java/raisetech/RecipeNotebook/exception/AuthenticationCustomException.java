package raisetech.RecipeNotebook.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationCustomException extends AuthenticationException {

  public AuthenticationCustomException(String message) {
    super(message);
  }

}
