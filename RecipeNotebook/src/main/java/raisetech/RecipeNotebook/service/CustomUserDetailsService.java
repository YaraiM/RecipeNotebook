package raisetech.RecipeNotebook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import raisetech.RecipeNotebook.data.User;
import raisetech.RecipeNotebook.exception.ResourceNotFoundException;
import raisetech.RecipeNotebook.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Autowired
  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws ResourceNotFoundException {
    User user = userRepository.findByUsername(username);

    if (user == null) {
      throw new ResourceNotFoundException("ユーザーが見つかりません");
    }
    return user;

  }

  public User getLoggedInUser() {
    return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

}
