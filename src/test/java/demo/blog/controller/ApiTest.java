package demo.blog.controller;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.blog.IntegrationTest;
import demo.blog.service.BlogService;
import demo.blog.service.ImageServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiTest extends IntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired private BlogService service;
    private MockMvc mvc;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void postLifecycleHonorsAllResponseFieldsAndPostReadMethod() throws Exception {
        String response = mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Java","text":"Full text","tags":["java"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(response).get("id").asLong();
        mvc.perform(post("/api/posts/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.text").value("Full text"))
                .andExpect(jsonPath("$.tags[0]").value("java"));
        mvc.perform(get("/api/posts/{id}", id)).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/api/posts/{id}/likes", id)).andExpect(content().string("1"));
        mvc.perform(put("/api/posts/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new PostRequest(id, "Updated", "Changed", List.of()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.likesCount").value(1));
        mvc.perform(get("/api/posts").param("search", "").param("pageNumber", "1").param("pageSize", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.hasPrev").value(false)).andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.lastPage").value(1));
        mvc.perform(delete("/api/posts/{id}", id)).andExpect(status().isOk());
        mvc.perform(post("/api/posts/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void commentLifecycleChecksPathAndBodyIds() throws Exception {
        long postId = service.create("Title", "Text", List.of()).id();
        String result = mvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CommentRequest(null, "Comment", postId))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.postId").value(postId))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(result).get("id").asLong();
        mvc.perform(get("/api/posts/{p}/comments/{id}", postId, id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.text").value("Comment"));
        mvc.perform(get("/api/posts/{p}/comments", postId))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(put("/api/posts/{p}/comments/{id}", postId, id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CommentRequest(id, "Edited", postId))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.text").value("Edited"));
        mvc.perform(put("/api/posts/{p}/comments/{id}", postId, id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CommentRequest(id, "Wrong post", postId + 1))))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/posts/{p}/comments/{id}", postId, id)).andExpect(status().isOk());
        mvc.perform(get("/api/posts/{p}/comments/{id}", postId, id)).andExpect(status().isNotFound());
    }

    @Test
    void multipartPutStoresAndReturnsExactImageBytes() throws Exception {
        long id = service.create("Image", "Text", List.of()).id();
        byte[] png = ImageServiceTest.png();
        var image = new MockMultipartFile("image", "../../untrusted.png", "application/octet-stream", png);
        mvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", id).file(image))
                .andExpect(status().isOk());
        mvc.perform(get("/api/posts/{id}/image", id)).andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(png));
        mvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", id)
                        .file(new MockMultipartFile("image", new byte[0])))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", id)
                        .file(new MockMultipartFile("file", png)))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/posts/{id}", id)).andExpect(status().isOk());
        mvc.perform(get("/api/posts/{id}/image", id)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsMissingInvalidAndMismatchedFields() throws Exception {
        mvc.perform(get("/api/posts")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/posts").param("search", "").param("pageNumber", "0").param("pageSize", "5"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"text\":\"y\",\"tags\":[null]}"))
                .andExpect(status().isBadRequest());
        long id = service.create("Title", "Text", List.of()).id();
        mvc.perform(put("/api/posts/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"text\":\"y\",\"tags\":[]}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/posts/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new PostRequest(id + 1, "x", "y", List.of()))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/posts/{id}/comments", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"comment\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void corsAllowsFrontendButRejectsOtherOrigins() throws Exception {
        mvc.perform(options("http://localhost:8080/api/posts/1/image").header("Origin", "http://localhost")
                        .header("Access-Control-Request-Method", "PUT"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost"));
        mvc.perform(options("http://localhost:8080/api/posts").header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
