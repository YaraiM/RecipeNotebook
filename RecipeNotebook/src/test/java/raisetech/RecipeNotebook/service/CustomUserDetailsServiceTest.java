package raisetech.RecipeNotebook.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import raisetech.RecipeNotebook.data.User;
import raisetech.RecipeNotebook.exception.ResourceNotFoundException;
import raisetech.RecipeNotebook.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CustomUserDetailsService sut;

  @Test
  void 正常系_ユーザー名を渡してメソッドが実行されること() {
    User user = new User();
    String username = "user";
    user.setUsername(username);

    when(userRepository.findByUsername(username)).thenReturn(user);

    sut.loadUserByUsername(user.getUsername());

    verify(userRepository, times(1)).findByUsername(anyString());
  }

  @Test
  void 異常系_存在しないユーザー名を渡した場合に例外が発生すること() {
    String username = "notExistingUsername";

    when(userRepository.findByUsername(username)).thenReturn(null);

    ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
        () -> sut.loadUserByUsername(username));
    assertThat(e.getMessage(), is("ユーザーが見つかりません"));

    verify(userRepository, times(1)).findByUsername(anyString());
  }

}
