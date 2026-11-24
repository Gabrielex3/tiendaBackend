package com.exe.productservice.Controller;
import com.exe.productservice.Model.Blog;
import com.exe.productservice.Repository.BlogRepository;
import com.exe.productservice.FeignClient.UserServiceFeignClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.List;
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class BlogController {
 private final BlogRepository repository;
 private final UserServiceFeignClient users;
 public record BlogRequest(@NotBlank @Size(max=255) String title, @NotBlank @Size(max=1000) String summary, @NotBlank @Size(max=2000) String image, @NotBlank @Size(max=50000) String content) {}
 private void admin(String token) {
  if(token==null || !token.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
  try { if(!"ADMIN".equals(String.valueOf(users.getUsuarioPerfil(token).get("role")))) throw new ResponseStatusException(HttpStatus.FORBIDDEN); }
  catch(feign.FeignException e) { throw new ResponseStatusException(e.status()==401 || e.status()==403 ? HttpStatus.UNAUTHORIZED : HttpStatus.SERVICE_UNAVAILABLE); }
 }
 @GetMapping("/blogs") public List<Blog> list(){return repository.findAll(Sort.by(Sort.Direction.DESC,"id"));}
 @GetMapping("/blogs/{id}") public Blog get(@PathVariable Long id){return repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private Blog save(Blog blog, BlogRequest data){blog.setTitle(data.title().trim());blog.setSummary(data.summary().trim());blog.setImage(data.image().trim());blog.setContent(data.content().trim());return repository.save(blog);}
 @PostMapping("/admin/blogs") @ResponseStatus(HttpStatus.CREATED)
 public Blog create(@RequestHeader(value="Authorization",required=false) String token,@Valid @RequestBody BlogRequest data){admin(token);Blog blog=new Blog();blog.setDate(LocalDate.now());return save(blog,data);}
 @PutMapping("/admin/blogs/{id}") public Blog update(@RequestHeader(value="Authorization",required=false) String token,@PathVariable Long id,@Valid @RequestBody BlogRequest data){admin(token);return save(get(id),data);}
 @DeleteMapping("/admin/blogs/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void delete(@RequestHeader(value="Authorization",required=false) String token,@PathVariable Long id){admin(token);repository.delete(get(id));}
}
