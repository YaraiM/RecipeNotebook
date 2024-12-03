package raisetech.RecipeNotebook.repository;

import org.apache.ibatis.annotations.Mapper;
import raisetech.RecipeNotebook.data.User;

@Mapper
public interface UserRepository {

  User findByUsername(String username);

}
