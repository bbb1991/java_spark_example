import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class BlogPostDAO {
    private MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    public Document findByPermalink(String permalink) {
        return postsCollection.find(new Document("permalink", permalink)).first();
    }

    public List<Document> findByDateDescending(int limit) {

        return postsCollection.find().sort(new Document("date", -1)).limit(limit).into(new ArrayList<Document>());
    }

    public List<Document> findByTagDateDescending(final String tag) {

//        BasicDBObject query = new BasicDBObject("tags", tag);
        Bson filter = in("tags",tag);

        //System.out.println("/tag query: " + filter.toBsonDocument(Document.class,new Co).toJson());
        List<Document> posts = postsCollection.find(filter).sort(new Document("date", -1))
                .limit(10).into(new ArrayList<Document>());
        System.out.println("For tag: "+tag);

        return posts;
    }

    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();

        String permLinkExtra = String.valueOf(GregorianCalendar
                .getInstance().getTimeInMillis());
        permalink += permLinkExtra;

        Document post = new Document("title", title);
        post.append("author", username);
        post.append("body", body);
        post.append("permalink", permalink);
        post.append("tags", tags);
        post.append("comments", new java.util.ArrayList());
        post.append("date", new java.util.Date());

        try {
            postsCollection.insertOne(post);
            System.out.println("Inserting blog post with permalink " + permalink);
        } catch (Exception e) {
            System.out.println("Error inserting post");
            return null;
        }

        return permalink;
    }

    public void addPostComment(final String name, final String email, final String body, final String permalink) {
        Document comment = new Document("author", name)
                .append("body", body);
        if (email != null && !email.equals("")) {
            comment.append("email", email);
        }

        UpdateResult result = postsCollection.updateOne(new Document("permalink", permalink),
                new Document("$push",
                        new Document("comments", comment)));

        System.out.println("Matches: " +result.getMatchedCount());
        System.out.println("Modified: " + result.getModifiedCount());
    }

}
