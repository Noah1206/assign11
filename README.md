Project Structure & Spring MVC

PostController, PostService, PostRepository는 각각 어떤 역할을 하며, 서로 어떻게 연결되는가?
-> PostController는 HTTP로 클라이언트와 통신하고, 웹 페이지를 클라이언트에게 전송하는 역할을 한다. 또한 Service에게 데이터를 전송하고, 결과를 반환하기도 한다.
PostService는 Controller에게 전해받은 데이터를 가지고, 비즈니스 로직을 처리하고, 데이터 저장을 위해 PostRepository에게 데이터를 전해주기도 한다. PostRepository는 직접 데이터를 DB와 연결하여 실제로 테이블을 생성하고, 저장하고, 수정하는 역할을 한다. 이 작업이 잘됐는지 안됐는지에 대한 내용도 Service에게 보내주기도 한다.
-> 이 3가지 역할들은 서로 데이터를 가지고 연결된다.
게시글 작성 시 HTML Form의 입력값과 첨부파일은 Controller에 어떻게 전달되는가?
먼저 게시글 작성시, HTML Form에 데이터와 첨부파일은 각각 Controller에게 multipart/form-data를 기반으로 HTTP로 함께 서버로 controller에게 전송된다.  

JPA & Entity Relationship

Post와 Attachment Entity는 어떤 관계이며, @ManyToOne과 @OneToMany는 각각 어떤 역할을 하는가?
Post는 하나의 게시물을 나타내고, Attachment Entity는 그 게시물의 메타데이터를 의미하고, Post -> Attachment Entity들 이렇게 하나에서 여러개를 가질 수 있는 관계이고, 
이때 ManyToOne 관계를 Attachment Entity에 나타낸다. 하나의 게시물이 여러개의 첨부파일을 가질 수 있다는 것처럼, 여러개의 첨부파일이 하나의 게시물을 나타낼 수 있는데, 그게 OneToMany이다.

cascade = CascadeType.ALL과 orphanRemoval = true는 어떤 역할을 하며, 실제 디스크에 저장된 파일에도 영향을 주는가?
cascade = CascadeType.ALL은 영속성을 부모와 자식 엔티티 전부 연속시키는 역할을 하고, orphanRemoval은 부모 엔티티를 삭제했을때, 관계가 끊어졌을때 자식 엔티티만 덩그러니 혼자 남는게 아니라, 같이 사라지게 하는 코드입니다.
실제 디스크에 저장된 파일에는 JPA가 작업하는 범위가 아니고, 엔티티 범위에서 DB 데이터를 관리하기 때문에 영속성 종속이 되어있는 범위까지만 삭제시킨다.

File Upload

MultipartFile은 무엇이며, 업로드된 파일은 어떤 과정을 거쳐 서버에 저장되는가?
클라이언트에게 서버로 파일을 전송받을때, 텍스트와 파일을 같이 서버로 HTTP 를 통해 받을때 사용하는 개념이 MultipartFile이다.
업로드된 파일은 먼저 multipart/form-data형식으로 클라이언트에게 파일과 텍스트 등등의 데이터들을 전송받고, 파일의 메타데이터를 가진 파일과 원본 파일로 나누고, 파일의 이름을 UUID로 바꾼후 사용한다. 그후에 데이터 베이스에는 메타데이터 파일을 저장하고, 실제 디스크에 원본 파일을 저장한다.
파일을 원본 이름 대신 UUID 기반 파일명으로 저장하는 이유는 무엇인가?
UUID 기반 파일명으로 했을때, 고유한 이름이기 때문에 다른 사용자가 같은 이름을 가진 파일을 서버에 업로드했을때 중복되지 않게 할 수 있고, 원본 파일의 이름도 존재하기 때문에, 안전성면에서도 보안을 높일 수 있다.

File Download & Security

파일 다운로드 시 Resource, ResponseEntity, Content-Disposition은 각각 어떤 역할을 하는가?
return ResponseEntity.ok()
.contentType(MediaType.APPLICATION_OCTET_STREAM)
.header(HttpHeaders.CONTENT_DISPOSITION, disposition)
.body(resource);

파일 다운로드시, Resourse는 파일 등을 추상화하는 데이터 객체를 나타낸다. ResponseEntity는 HTTP의 응답 코드를 말하고, 현재 코드처럼 ok 라고 된다면, 200을 반환한다.
Content-Disposition은 헤더에 클라이언트에서 전송하는 데이터를 어떤 형식으로 처리할지를 나타내는 것
파일 저장 과정에서 normalize()와 startsWith()를 사용하는 이유는 무엇인가?
uploadDir = Path.of(path).toAbsolutePath().normalize();
먼저 이 경로에 불필요한 부분을 지워주는 normalize()함수를 사용하고,
if (!target.startsWith(uploadDir)) throw new IllegalArgumentException("잘못된 파일 경로입니다.");
지정된 경로로 들어가는 Path인지 확인할때 startsWith()를 사용해서, Path Traversal을 방지하는데 도움이 된다.
관련 소스파일 및 메서드명을 함께 명시
각 문항은 2~4문장 정도로 간단히 설명


다음 3가지 유형을 각각 1곳 이상 수정합니다.

Text : 제목
Style : 색상
UI/Data : 파일 크기 표시

STEP 4. New Feature Implementation

이미지 미리보기(Image Preview) 기능을 구현합니다.

게시글 상세 화면에서 첨부 이미지(jpg, jpeg, png, gif) 표시
이미지가 아닌 파일(PDF 등)은 기존 다운로드 기능 유지
존재하지 않는 파일 또는 잘못된 파일 ID에 대한 오류 처리
구현 참고: AttachmentController, Service, Resource, Content-Type, Thymeleaf <img> 활용

※ Sample Project의 TODO를 참고하여 직접 구현합니다.