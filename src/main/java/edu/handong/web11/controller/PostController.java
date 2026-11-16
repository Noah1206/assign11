package edu.handong.web11.controller;

import edu.handong.web11.domain.Post;
import edu.handong.web11.dto.PostForm;
import edu.handong.web11.service.PostService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class PostController {
    private final PostService posts;

    public PostController(PostService posts) {
        this.posts = posts;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/posts";
    }

    @GetMapping("/posts")
    public String list(Model model) {
        model.addAttribute("posts", posts.all());
        return "posts/list";
    }

    @GetMapping("/posts/new") // GET요청으로 게시글 작성 페이지로 들어오는 요청을 처리한다.
    public String form(Model model) {
        model.addAttribute("postForm", new PostForm()); // PostForm을 postForm이라는 이름을 가진 객체로 서버에서 뷰로 전송한다.
        return "posts/form"; // form 페이지로 이동한다.
    }

    @PostMapping("/posts") // post 요청으로 게시글 작성 form을 제출할때 나타난다.
    public String create(@Valid @ModelAttribute("postForm") PostForm form, BindingResult errors, // modelAttribute 를 통해, postForm 이라는 이름을 가진 HTML에서 전송한 Form 데이터를 가진 form 객체를 생성한다. 또한 Valid 어노테이션을 통해 유효검사를 실시한다.
                         @RequestParam(name = "files", required = false) List<MultipartFile> files) {  // 필수적으로 받는 건 아니지만, Form으로 file까지 같이 받아온다.
        if (errors.hasErrors()) return "posts/form"; // 이렇게 전달받은 데이터에서 오류가 나타나면 바로 form데이터를 입력하는 페이지로 다시 넘어간다.
        Long id = posts.create(form, files == null ? List.of() : files); // 파일이 존재할 때는 files를 포함한 form 데이터를 가지고 id를 create하고, 아니라면 List.of()를 포함한 form 데이터로 id를 create한다.
        return "redirect:/posts/" + id; // 위에서 생성시킨 id를 가지고, post 링크와 함께 리다이렉트한다.
    }

    @GetMapping("/posts/{id}") // GET HTTP 메서드로 요청을 받아온다. 리다이렉션한 생성 id를 받아온다.
    public String detail(@PathVariable Long id, Model model) { // id 값을 가지고오고, 조회한 게시물의 데이터를 뷰로 전달하기 위해서 가지고온다.
        model.addAttribute("post", posts.get(id)); // posts.get(id)로 생성된 게시글의 객체를 post라는 이름으로 가지고온다.
        return "posts/detail"; // post라는 객체를 함께 서버에서 detail 뷰로 전송한다.
    }

    @GetMapping("/posts/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Post post = posts.get(id);
        PostForm form = new PostForm();
        form.setTitle(post.getTitle());
        form.setContent(post.getContent());
        model.addAttribute("postForm", form);
        model.addAttribute("postId", id);
        return "posts/form";
    }

    @PostMapping("/posts/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("postForm") PostForm form, BindingResult errors,
                         Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("postId", id);
            return "posts/form";
        }
        posts.update(id, form);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        posts.delete(id);
        return "redirect:/posts";
    }
}
