import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import sun.misc.BASE64Encoder;

import java.security.SecureRandom;

public class SessionDAO {

    private final MongoCollection<Document> sessionsCollection;

    public SessionDAO(final MongoDatabase blogDatabase) {
        this.sessionsCollection = blogDatabase.getCollection("sessions");
    }


    /**
     * Поиск в базе пользователя по id и возвращения поля username
     * @param sessionId id сессий
     * @return
     */
    public String findUserNameBySessionId(final String sessionId) {
        Document session = getSession(sessionId);

        if (session != null) {
            return session.get("username").toString();
        }
        return null;
    }


    /**
     * Создание сессий для пользователя
     * @param username логин, на которую нужно создать сессию
     * @return новая сессия
     */
    public Document startSession(final String username) {

        // Генерируем новый ID сессий
        SecureRandom generator = new SecureRandom();
        byte randomBytes[] = new byte[32];
        generator.nextBytes(randomBytes);

        BASE64Encoder encoder = new BASE64Encoder();
        String sessionID = encoder.encode(randomBytes);

        // создание нового BSON обьекта
        Document session = new Document("username", username);
        session.append("_id", sessionID);

        // удаление старых сессий, если остались и вставка новой
        sessionsCollection.deleteMany(new Document("username",username));
        sessionsCollection.insertOne(session);

        return session;
    }

    /**
     * Удаление сессий пользователя
     * @param sessionID
     */
    public void endSession(final String sessionID) {
        sessionsCollection.deleteOne(new Document("_id", sessionID));
    }

    /**
     * Получение сессий по id
     * @param sessionID  id сессий
     * @return сессия пользователя
     */
    public Document getSession(final String sessionID) {
        return sessionsCollection.find(new Document("_id", sessionID)).first();
    }
}
