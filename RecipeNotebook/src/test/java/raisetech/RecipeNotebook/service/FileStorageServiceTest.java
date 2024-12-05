package raisetech.RecipeNotebook.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import raisetech.RecipeNotebook.exception.FileStorageException;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

  @Mock
  private MultipartFile mockFile;

  @InjectMocks
  private FileStorageService sut;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(sut, "uploadDir", "test-uploads");
  }

  @Test
  void ファイルの保存_正常系_ファイルが正しく保存されること() throws IOException {
    String originalFilename = "test.jpg";
    when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));

    String actual = sut.storeFile(mockFile);

    assertThat(actual, allOf(
        startsWith("/uploads/"),
        containsString("_" + originalFilename),
        matchesPattern("/uploads/[\\w-]+_test\\.jpg")
    ));

    String fileName = actual.substring(
        "/uploads/".length()); //storeFileメソッドで書き込まれるuploadsという名前の部分を除く
    Path createdFile = Paths.get("test-uploads", fileName);
    assertThat(Files.exists(createdFile), is(true));

    Files.deleteIfExists(Paths.get("test-uploads", actual.substring("/uploads/".length())));
    Files.deleteIfExists(Paths.get("test-uploads"));
  }

  @Test
  void ファイルの保存_正常系_ファイルがnullの場合にNoImage画像のパスが返されること() {
    String result = sut.storeFile(null);

    assertThat(result, is("/images/no_image.jpg"));
  }

  @Test
  void ファイルの保存_異常系_FileStorageExceptionがスローされること()
      throws IOException {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
    when(mockFile.getInputStream()).thenThrow(new IOException("Test exception"));

    FileStorageException exception = assertThrows(FileStorageException.class,
        () -> sut.storeFile(mockFile));

    assertThat(exception.getMessage(), is("ファイルの保存に失敗しました"));
  }

  @Test
  void ファイルの削除_ファイルが正しく削除されること() throws IOException {
    Path uploadPath = Paths.get("test-uploads");
    Files.createDirectories(uploadPath);
    Path testFile = uploadPath.resolve("test.jpg");
    Files.write(testFile, "test data".getBytes());

    sut.deleteFile("/test-uploads/test.jpg");

    assertThat(Files.exists(testFile), is(false));

    Files.deleteIfExists(uploadPath);
  }

}
