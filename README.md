Project Structure & Spring MVC

PostController, PostService, PostRepository는 각각 어떤 역할을 하며, 서로 어떻게 연결되는가?
-> PostController는 HTTP로 클라이언트와 통신하고, 웹 페이지를 클라이언트에게 전송하는 역할을 한다. 또한 Service에게 데이터를 전송하고, 결과를 반환하기도 한다.
PostService는 Controller에게 전해받은 데이터를 가지고, 비즈니스 로직을 처리하고, 데이터 저장을 위해 PostRepository에게 데이터를 전해주기도 한다. PostRepository는 직접 데이터를 DB와 연결하여 실제로 테이블을 생성하고, 저장하고, 수정하는 역할을 한다. 이 작업이 잘됐는지 안됐는지에 대한 내용도 Service에게 보내주기도 한다.
-> 이 3가지 역할들은 서로 데이터를 가지고 연결된다.
게시글 작성 시 HTML Form의 입력값과 첨부파일은 Controller에 어떻게 전달되는가?
먼저 게시글 작성시, HTML Form에 데이터와 첨부파일은 각각 Controller에게 multipart/form-data를 기반으로 HTTP로 함께 서버로 controller에게 전송된다.  

JPA & Entity Relationship

Post와 Attachment Entity는 어떤 관계이며, @ManyToOne과 @OneToMany는 각각 어떤 역할을 하는가?


cascade = CascadeType.ALL과 orphanRemoval = true는 어떤 역할을 하며, 실제 디스크에 저장된 파일에도 영향을 주는가?