import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bson.Document;
import spark.ModelAndView;
import spark.Request;
import spark.Spark;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BlogController {

    private static MongoClient mongo = new MongoClient();
    private static MongoDatabase database = mongo.getDatabase("blog");
    private static Configuration configuration;

    private static UserDAO userDAO;
    private static SessionDAO sessionDAO;
    private static BlogPostDAO blogPostDAO;
    private static Document user = null;
    private static final int MAX_POST_COUNT = 10;


    /**
     * Конструктор с аргументом
     * @param configuration конфигурация Freemarker
     * @throws IOException
     */
    public BlogController(Configuration configuration) throws IOException {
        BlogController.configuration = configuration;
        userDAO = new UserDAO(database);
        sessionDAO = new SessionDAO(database);
        blogPostDAO = new BlogPostDAO(database);
        initializeRoutes();
    }

    private void initializeRoutes() throws IOException {

        Spark.externalStaticFileLocation("/static");
        // Домашняя страница блога
        Spark.get("/", (req, resp) -> {

            String username = sessionDAO.findUserNameBySessionId(getSessionCookie(req));

            List<Document> posts = blogPostDAO.findByDateDescending(MAX_POST_COUNT);
            SimpleHash root = new SimpleHash();

            root.put("myposts", posts);

            if (username != null) {
                root.put("username", username);
            }

            return new ModelAndView(root, "blog.ftl");
        }, new FreeMarkerEngine(configuration));

        // Страница для отображения конкретного поста
        Spark.get("/post/:permalink", (req, resp) -> {
            String permalink = req.params(":permalink");

            System.out.println("/post get: " + permalink);

            Document post = blogPostDAO.findByPermalink(permalink);

            // TODO change halt() to redirect()
            if (post == null) {
                Spark.halt(404, "Post not found, sorry...");
            }

            SimpleHash newComment = new SimpleHash();
            newComment.put("name", "");
            newComment.put("email", "");
            newComment.put("body", "");

            SimpleHash root = new SimpleHash();

            root.put("post", post);
            root.put("comments", newComment);

            return new ModelAndView(root, "entry.ftl");
        }, new FreeMarkerEngine(configuration));

        // Метод для добавления комментов
        Spark.post("/newcomment", (request, response) -> {

            String name = StringEscapeUtils.escapeHtml4(request.queryParams("commentName"));
            String email = StringEscapeUtils.escapeHtml4(request.queryParams("commentEmail"));
            String body = StringEscapeUtils.escapeHtml4(request.queryParams("commentBody"));
            String permalink = request.queryParams("permalink");

            Document post = blogPostDAO.findByPermalink(permalink);
            if (post == null) {
                response.redirect("/post_not_found");
                Spark.halt(301);
                return null;
            }
            // check that comment is good
            else if (name.equals("") || body.equals("")) {
                // bounce this back to the user for correction
                SimpleHash root = new SimpleHash();
                SimpleHash comment = new SimpleHash();

                comment.put("name", name);
                comment.put("email", email);
                comment.put("body", body);
                root.put("comment", comment);
                root.put("post", post);
                root.put("errors", "Post must contain your name and an actual comment");

                return new ModelAndView(root, "entry.ftl");
            } else {
                blogPostDAO.addPostComment(name, email, body, permalink);
                response.redirect("/post/" + permalink);
                Spark.halt(301);
                return null;
            }
        }, new FreeMarkerEngine(configuration));


        Spark.get("/error", (req, resp) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("error", "The server made a boo-boo...");

            return new ModelAndView(map, "error.ftl");
        }, new FreeMarkerEngine(configuration));


        Spark.get("/login", (req, resp) -> {
            String username = sessionDAO.findUserNameBySessionId(getSessionCookie(req));

            SimpleHash root = new SimpleHash();

            if (username != null) {
                root.put("username", username);
            }

            return new ModelAndView(root, "login.ftl");
        }, new FreeMarkerEngine(configuration));

        Spark.post("/login", (req, resp) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");


            System.out.println(String.format("Пользователь %s пытается  авторизоваться", username));
            user = userDAO.validateLogin(username, password);
            if (user == null) {
                Spark.halt(401, "Go away!");
            }


            // valid user, let's log them in
            String sessionID = sessionDAO.startSession(user.get("username").toString()).getString("_id");

            if (sessionID == null) {
                resp.redirect("/error");
                Spark.halt(301);
            } else {
                // set the cookie for the user's browser
                resp.raw().addCookie(new Cookie("session", sessionID));

                resp.redirect("/welcome");
                Spark.halt(301);
            }

            resp.redirect("/hello", 301);
            return null;
        });

        Spark.get("/newpost", (req, resp) -> {
            String username = sessionDAO.findUserNameBySessionId(getSessionCookie(req));
            if (username == null) {
                resp.redirect("/login");
                Spark.halt(301);
            }
            SimpleHash root = new SimpleHash();
            root.put("username", username);

            return new ModelAndView(root, "new_post.ftl");
        }, new FreeMarkerEngine(configuration));

        Spark.get("/tag/:thetag", (request, response) -> {

            String username = sessionDAO.findUserNameBySessionId(getSessionCookie(request));
            SimpleHash root = new SimpleHash();

            String tag = StringEscapeUtils.escapeHtml4(request.params(":thetag"));
            List<Document> posts = blogPostDAO.findByTagDateDescending(tag);

            root.put("myposts", posts);
            if (username != null) {
                root.put("username", username);
            }

            return new ModelAndView(root, "blog.ftl");
        }, new FreeMarkerEngine(configuration));

        Spark.get("/welcome", (request, response) -> {
            String cookie = getSessionCookie(request);
            String username = sessionDAO.findUserNameBySessionId(cookie);

            if (username == null) {
                System.out.println("welcome() can't identify the user, redirecting to login");
                response.redirect("/login");
                Spark.halt(301);
                return null;

            } else {
                SimpleHash root = new SimpleHash();

                root.put("username", username);

                return new ModelAndView(root, "welcome.ftl");
            }
        }, new FreeMarkerEngine(configuration));

        Spark.post("/newpost", (req, resp) -> {
            String title = StringEscapeUtils.escapeHtml4(req.queryParams("subject"));
            String post = StringEscapeUtils.escapeHtml4(req.queryParams("body"));
            String tags = StringEscapeUtils.escapeHtml4(req.queryParams("tags"));

            String username = sessionDAO.findUserNameBySessionId(getSessionCookie(req));

            if (username == null) {
                resp.redirect("/login");
                Spark.halt(301);
            }


            if (title.equals("") || post.equals("")) {
                // redisplay page with errors
                HashMap<String, String> root = new HashMap<>();
                root.put("errors", "post must contain a title and blog entry.");
                root.put("subject", title);
                root.put("username", username);
                root.put("tags", tags);
                root.put("body", post);
                return new ModelAndView(root, "new_post.ftl");
            } else {
                // extract tags
                ArrayList<String> tagsArray = extractTags(tags);

                // substitute some <p> for the paragraph breaks
                post = post.replaceAll("\\r?\\n", "<p>");

                String permalink = blogPostDAO.addPost(title, post, tagsArray, username);

                // now redirect to the blog permalink
                resp.redirect("/post/" + permalink);

                Spark.halt(301);
                return null;

            }
        }, new FreeMarkerEngine(configuration));

        Spark.get("/logout", (req, resp) -> {
            String sessionID = getSessionCookie(req);

            if (sessionID == null) {
                // no session to end
                resp.redirect("/login");
                return null;
            } else {
                // deletes from session table
                sessionDAO.endSession(sessionID);

                // this should delete the cookie
                Cookie c = getSessionCookieActual(req);
                if (c != null) {
                    c.setMaxAge(0);
                }

                resp.raw().addCookie(c);

                resp.redirect("/login");
                Spark.halt(301);
                return null;
            }
        }, new FreeMarkerEngine(configuration));

    }

    private String getSessionCookie(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session")) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // helper function to get session cookie as string
    private Cookie getSessionCookieActual(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session")) {
                return cookie;
            }
        }
        return null;
    }

    // работа с тэгами
    private ArrayList<String> extractTags(String tags) {

        tags = tags.replaceAll("\\s", "");
        String tagArray[] = tags.split(",");

        ArrayList<String> cleaned = new ArrayList<>();
        for (String tag : tagArray) {
            if (!tag.equals("") && !cleaned.contains(tag)) {
                cleaned.add(tag);
            }
        }

        return cleaned;
    }
}
