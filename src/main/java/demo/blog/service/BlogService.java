package demo.blog.service;

import java.util.List;

import demo.blog.dao.CommentRepository;
import demo.blog.dao.PostRepository;
import demo.blog.model.Comment;
import demo.blog.model.Post;
import demo.blog.model.PostImage;
import demo.blog.model.PostPage;
import demo.blog.model.SearchFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BlogService {
    private final PostRepository posts;
    private final CommentRepository comments;

    public BlogService(PostRepository posts, CommentRepository comments) {
        this.posts = posts;
        this.comments = comments;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PostPage list(String search, int pageNumber, int pageSize) {
        if (pageNumber < 1 || pageSize < 1 || pageSize > 100) {
            throw new BadRequestException("pageNumber must be positive; pageSize must be between 1 and 100");
        }
        SearchFilter filter = SearchFilter.parse(search);
        long count = posts.count(filter);
        long lastPage = Math.max(1, (count + pageSize - 1) / pageSize);
        List<Post> page = posts.findPage(filter, (long) (pageNumber - 1) * pageSize, pageSize);
        return new PostPage(page.stream().map(Post::preview).toList(), pageNumber > 1,
                pageNumber < lastPage, lastPage);
    }

    public Post get(long id) {
        return posts.find(id).orElseThrow(() -> new NotFoundException("Post"));
    }

    @Transactional
    public Post create(String title, String text, List<String> tags) {
        long id = posts.create(title, text, normalizeTags(tags));
        return get(id);
    }

    @Transactional
    public Post update(long id, String title, String text, List<String> tags) {
        if (!posts.update(id, title, text, normalizeTags(tags))) {
            throw new NotFoundException("Post");
        }
        return get(id);
    }

    @Transactional
    public void delete(long id) {
        if (!posts.delete(id)) {
            throw new NotFoundException("Post");
        }
    }

    @Transactional
    public long like(long id) {
        return posts.incrementLikes(id).orElseThrow(() -> new NotFoundException("Post"));
    }

    public PostImage image(long id) {
        requirePost(id);
        return posts.findImage(id).orElseThrow(() -> new NotFoundException("Image"));
    }

    @Transactional
    public void saveImage(long id, PostImage image) {
        requirePost(id);
        posts.saveImage(id, image);
    }

    public List<Comment> comments(long postId) {
        requirePost(postId);
        return comments.findAll(postId);
    }

    public Comment comment(long postId, long id) {
        return comments.find(postId, id).orElseThrow(() -> new NotFoundException("Comment"));
    }

    @Transactional
    public Comment addComment(long postId, String text) {
        requirePost(postId);
        return comments.create(postId, text);
    }

    @Transactional
    public Comment updateComment(long postId, long id, String text) {
        if (!comments.update(postId, id, text)) {
            throw new NotFoundException("Comment");
        }
        return new Comment(id, text, postId);
    }

    @Transactional
    public void deleteComment(long postId, long id) {
        if (!comments.delete(postId, id)) {
            throw new NotFoundException("Comment");
        }
    }

    private void requirePost(long id) {
        if (!posts.exists(id)) {
            throw new NotFoundException("Post");
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        return tags.stream().map(String::strip).distinct().toList();
    }
}