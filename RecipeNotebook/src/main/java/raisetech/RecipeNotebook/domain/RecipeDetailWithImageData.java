package raisetech.RecipeNotebook.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * レシピ詳細情報にBase64の画像データを付与したオブジェクトです。
 */
@Schema(description = "レシピ詳細情報＋画像データ（Base64）")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDetailWithImageData {

  @Valid
  private RecipeDetail recipeDetail;

  private String imageData;

  public MultipartFile convertBase64ToMultipartFile() {
    if (imageData == null || imageData.isEmpty()) {
      return null;
    }

    try {
      String base64Image = imageData.split(",")[1];
      byte[] imageBytes = Base64.getDecoder().decode(base64Image);

      return new CustomMultipartFile(imageBytes);
    } catch (Exception e) {
      throw new RuntimeException("Base64イメージの変換に失敗", e);
    }
  }

  private record CustomMultipartFile(byte[] fileContent) implements MultipartFile {

    @Override
    public String getName() {
      return "file";
    }

    @Override
    public String getOriginalFilename() {
      return "image.jpg";
    }

    @Override
    public String getContentType() {
      return "image/jpeg";
    }

    @Override
    public boolean isEmpty() {
      return fileContent == null || fileContent.length == 0;
    }

    @Override
    public long getSize() {
      return fileContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
      return fileContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
      try (FileOutputStream fos = new FileOutputStream(dest)) {
        fos.write(fileContent);
      }
    }

    @Override
    public Resource getResource() {
      return new ByteArrayResource(fileContent);
    }
  }
}
