import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import spark.ModelAndView;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;


public class Main {

    private static MongoClient mongo = new MongoClient();
    private static MongoDatabase database = mongo.getDatabase("test");
    private static MongoCollection<Document> collection = database.getCollection("users");

    public static void main(String[] args) {

        Document document = collection.find().first();
        Spark.get("/hello", (req, resp) -> {

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("username", document.get("name"));

            return new ModelAndView(arguments, "hello.ftl");
        }, new FreeMarkerEngine());

        Spark.exception(IllegalArgumentException.class, (e, req, resp) -> {
                resp.status(500);
                resp.body("<h1 align='center'>The server made a boo-boo...</h1>");
        }) ;

    }
}
