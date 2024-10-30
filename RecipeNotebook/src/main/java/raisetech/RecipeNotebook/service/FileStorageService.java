package raisetech.RecipeNotebook.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import raisetech.RecipeNotebook.exception.FileStorageException;

@Service
public class FileStorageService {

  @Value("${app.upload.dir}")
  private String uploadDir;

  public String storeFile(MultipartFile file) {

    try {
      if (file != null && !file.isEmpty()) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
          Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + filename;

      } else {
        return "/images/no_image.jpg";
      }

    } catch (IOException e) {
      throw new FileStorageException("ファイルの保存に失敗しました");
    }
  }
}
