package edu.handong.web11.service;

import edu.handong.web11.domain.Attachment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "pdf", "txt");
    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir}") String path) throws IOException {
        uploadDir = Path.of(path).toAbsolutePath().normalize(); // file을 업로드할 path 를 정규화시킨다.
        Files.createDirectories(uploadDir); // file을 넣을 path의 디렉토리를 생성한다.
    }

    public Attachment store(MultipartFile file) { // 업로드된 파일을 그대로 가지고온다.
        if (file.isEmpty()) throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다."); // 먼저 가지고온 파일이 비었는지 비지않았는지 확인한다.
        String original = file.getOriginalFilename(); // 원본 파일의 이름을 먼저 가지고온다.
        if (original == null || original.isBlank() || original.contains("/") || original.contains("\\") || original.contains("..")) // 파일 원본 이름의 Vaild를 체크합니다.
            throw new IllegalArgumentException("잘못된 파일 이름입니다.");
        if (original.length() > 240) throw new IllegalArgumentException("파일 이름이 너무 깁니다.");
        int dot = original.lastIndexOf('.'); // original 이름의 확장자 앞까지의 이름을 구분하기 위해서 lastIndexOf를 사용한다.
        String ext = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT); // Locale.ROOT를 사용함으로서, 사용자의 환경이 무엇이든 동일한 변환결과를 나타내고, original.substring을 사용함으로서 확장자 앞에 까지의 원본 이름까지의 이름을 잘라내고 배열로 만든다.
        if (!ALLOWED.contains(ext)) throw new IllegalArgumentException("허용 형식: jpg, jpeg, png, gif, pdf, txt"); // 허용 형식에 적혀져있는 확장자를 제외한 확장자는 못받게 한다.
        if (file.getSize() > 10L * 1024 * 1024) throw new IllegalArgumentException("파일은 10MB 이하만 가능합니다."); // 파일의 크기를 제한한다.
        String stored = UUID.randomUUID() + "." + ext; // UUID로 변환함으로서, 안정성과, 중복된 파일을 넣어도 사용가능하게 한다.
        Path target = uploadDir.resolve(stored).normalize(); // 방금 만든 파일 이름을 UUID로 변환한 내용을 앞서 구한 디렉토리 네임과 합치고, 이들을 정규화시킨다.
        if (!target.startsWith(uploadDir)) throw new IllegalArgumentException("잘못된 파일 경로입니다."); // 그리고 startsWith를 사용해서, 정해진 uploadDir이랑 맞는지 경로를 확인한다.
        try (InputStream input = file.getInputStream()) { // MultipartFile에 담은 file의 실제 데이터를 읽기위한 InputStream을 생성한다.
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING); // input파일의 내용을 target file에 복사하고, target file이 있다고 하더라도 REPLACE_EXISTING로 인해 덮어쓴다.
            // Teaching sample: MIME type is inferred by extension, not trusted from request headers.
            String contentType = switch (ext) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "pdf" -> "application/pdf";
                default -> "text/plain";
            }; // 확장자에 따라서 content file의 타입도 수정한다.
            return new Attachment(original, stored, contentType, file.getSize()); // 파일의 메타데이터를 객체로 변환한다.
        } catch (IOException e) {
            try { Files.deleteIfExists(target); } catch (IOException ignored) {}
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 실패", e);
        }
    }

    public Resource load(Attachment attachment) {
        Path target = uploadDir.resolve(attachment.getStoredFilename()).normalize(); // 저장된 파일 이름을 upload한 디렉토리 주소와 합친후 정규화합니다.
        if (!target.startsWith(uploadDir) || !Files.isRegularFile(target)) // 그래서 만약 디렉토리가 잘못됐거나, target이 디렉토리가 아닌 파일이라면 에러 처리할 수 있게 한다.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        return new FileSystemResource(target); // Resource로 반환한다.
    }

    public void deleteIfExists(Attachment attachment) {
        try {
            Files.deleteIfExists(uploadDir.resolve(attachment.getStoredFilename()).normalize());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 실패", e);
        }
    }
}
