import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import freemarker.template.Configuration;
import org.bson.Document;
import spark.ModelAndView;
import spark.Spark;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class BlogController {


    private static MongoClient mongo = new MongoClient();
    private static MongoDatabase database = mongo.getDatabase("blog");

    private static UserDAO userDAO;
    private static Document user = null;



    public BlogController() throws IOException {
        userDAO = new UserDAO(database);
        initializeRoutes();
    }

    private void initializeRoutes() throws IOException {

        Spark.get("/hello", (req, resp) -> {

            if (user == null) {
                resp.redirect("/login");
                Spark.halt(301);
            }

            Map<String, Object> arguments = new HashMap<>();

            arguments.put("username", user.get("username"));
            return new ModelAndView(arguments, "hello.ftl");
        }, new FreeMarkerEngine());


        // this is the blog home page
        Spark.get("/", (req, resp) -> {
            return new ModelAndView(null, "blog.ftl");
        }, new FreeMarkerEngine());


        Spark.get("/error", (req, resp) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("error", "The server made a boo-boo...");

            return new ModelAndView(map, "error.ftl");
        }, new FreeMarkerEngine());


        Spark.get("/login", (req, resp) -> {
            return new ModelAndView(null, "login.ftl");
        }, new FreeMarkerEngine());

        Spark.post("/login", (req, resp) -> {
            String username = req.queryParams("username");
            String password  = req.queryParams("password");

            System.out.println(String.format("Пользователь %s пытается  авторизоваться", username));
            user = userDAO.validateLogin(username, password);
            if (user == null) {
                Spark.halt(401, "Go away!");
            }

            resp.redirect("/hello", 301);
            return null;
        });

    }
}
