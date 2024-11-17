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

  @Value("${app.feature.someClassEnabled:true}")
  private boolean activeProfile;

  public String storeFile(MultipartFile file) {

    // CI環境時はアップロード操作を行わず、imagesディレクトリのno_imageのパスを返すよう設定
    if (!activeProfile) {
      return "/images/no_image.jpg";
    }

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

  public void deleteFile(String imagePath) {
    // CI環境時は削除操作を行わないよう設定
    if (activeProfile) {

      try {
        Path uploadPath = Paths.get(uploadDir);
        String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        Path filePath = uploadPath.resolve(fileName);

        // ディレクトリにファイルが存在する場合に限り削除。存在しなければ何もしないことを許容する。
        if (Files.exists(filePath)) {
          Files.delete(filePath);
        }

      } catch (IOException e) {
        throw new FileStorageException(
            "ファイルの削除に失敗しました");
      }
    }
  }
}

//TODO:no_imageの状態で登録されたレシピ画像に新たな画像を登録しようとすると、500エラーが発生する
