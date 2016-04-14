import freemarker.template.Configuration;

import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException {
        new BlogController(getConfiguration());
    }

    private static Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(FreeMarkerEngine.class, "");
        return configuration;
    }
}
