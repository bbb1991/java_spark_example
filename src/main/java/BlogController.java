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



public class BlogController {


    private static MongoClient mongo = new MongoClient();
    private static MongoDatabase database = mongo.getDatabase("test");
    private static MongoCollection<Document> collection = database.getCollection("users");



    public BlogController() throws IOException {

        initializeRoutes();
    }

    private void initializeRoutes() throws IOException {

        Spark.get("/hello", (req, resp) -> {

            Document document = collection.find().first();

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("username", document.get("name"));

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

    }
}
