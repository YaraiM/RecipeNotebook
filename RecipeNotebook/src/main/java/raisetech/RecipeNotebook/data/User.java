package raisetech.RecipeNotebook.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "ユーザー")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

  private int id;

  @NotBlank
  private String username;

  @NotBlank
  private String password;

  @Email
  private String email;

  @NotBlank
  private String role;

  @NotNull
  private boolean enabled;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

}
