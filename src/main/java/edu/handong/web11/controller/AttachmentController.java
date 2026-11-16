package edu.handong.web11.controller;

import edu.handong.web11.domain.Attachment;
import edu.handong.web11.repository.AttachmentRepository;
import edu.handong.web11.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentRepository attachments;
    private final FileStorageService storage;
    public AttachmentController(AttachmentRepository attachments, FileStorageService storage) {
        this.attachments = attachments; this.storage = storage;
    }
    @GetMapping("/{id}/download") // GET HTTP 요청을 처리한다.
    public ResponseEntity<Resource> download(@PathVariable Long id) { // 링크에 있는 id를 받아오고, HTTP 신호로 반환한다.
        Attachment file = attachments.findById(id).orElseThrow(() -> // attachments의 findById로 객체를 가지고옵니다. orElseThrow로 예외처리도 진행합니다.
            new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일이 없습니다.")); // 만약 없을때는 HTTP 신호로 바로 예외처리 에러로 보냅니다.
        Resource resource = storage.load(file); // file의 디렉토리를 포함한 주소를 정규화한 주소를 가지고온다.
        String disposition = ContentDisposition.attachment() // http 헤더값을 지정할 내용을 나타내고, .attachment를 통해서 파일을 첨부형태로 나타낸다.
            .filename(file.getOriginalFilename(), StandardCharsets.UTF_8) // 업로드할 file을 지정하는데, 기존의 파일명을 사용하고, 이때 파일명을 UTF-8로 인코딩하겠다고 한다.
            .build().toString(); // 이제 ContentDisposition 객체를 String 형식으로 생성한다.
        return ResponseEntity.ok() // HTTP 상태코드를 반환한다.
            .contentType(MediaType.APPLICATION_OCTET_STREAM) // 서버에서 클라이언트로 전송하는 데이터의 형식을 나타낸다.
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition) // 서버에서 클라이언트로 전송하는 데이터의 헤더에 넣을데이터로, 파일을 첨부파일 형태로 다운로드할 수 있게 한다.
            .body(resource); // 응답 본문에 전송할 파일이다.
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(@PathVariable Long id) {
        Attachment file = attachments.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일이 없습니다"));

        if (file.getContentType() == null || !file.getContentType().startsWith("image/"))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "이미지 파일 미리보기");

        Resource resource = storage.load(file);
        String disposition = ContentDisposition.inline()
            .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
            .build().toString();
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(file.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
            .body(resource);
    }
}
