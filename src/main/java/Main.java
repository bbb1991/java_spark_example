import spark.ModelAndView;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bbb1991 on 4/11/16.
 */
public class Main {
    public static void main(String[] args) {

        Spark.get("/hello", (req, resp) -> {
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("username", "John Smith");

            return new ModelAndView(arguments, "hello.ftl");
        }, new FreeMarkerEngine());

    }
}
