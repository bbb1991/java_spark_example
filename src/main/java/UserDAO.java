import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Класс для работы с пользователем(-ями).
 */
public class UserDAO {

    private final MongoCollection<Document> usersCollection;
    private Random random = new SecureRandom();

    /**
     * Конструктор
     * @param blogDatabase
     */
    public UserDAO(final MongoDatabase blogDatabase) {
        usersCollection = blogDatabase.getCollection("users");
    }

    /**
     * Проверка корректности пары логин/пароль
     * @param username введенный пользователем логин
     * @param password введенный пользователем пароль
     * @return обект Document с данными пользователя, если проверка прошло успешно
     * или null если какая либо из проверок зафейлен
     */
    public Document validateLogin(String username, String password) {
        // Берем данные о пользователе при помощи логина
        Document user = usersCollection.find(new Document("username", username)).first();

        // если такой логин нет в базе
        if (user == null) {
            System.err.println("Такой полльзователь не существует!");
            return null;
        }

        String hashedAndSalted = user.get("password").toString();

        String salt = hashedAndSalted.split(",")[1];

        System.out.println("Hash from password is: " + hashedAndSalted);

        if (!hashedAndSalted.equals(makePasswordHash(password, salt))) {
            System.err.println("Пароль неверный");
            return null;
        }

        return user;
    }


    /**
     * Метод для получения хэша из пароля, так как пароль в открытом виде не хранится
     * @param password введенный пользователем пароль
     * @param salt соль для получения хэша
     * @return хэш пароля
     */
    private String makePasswordHash(final String password, final String salt) {
        try {
            String saltedAndHashed = password + "," + salt;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(saltedAndHashed.getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            byte hashedBytes[] = (new String(digest.digest(), "UTF-8")).getBytes();
            return encoder.encode(hashedBytes) + "," + salt;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 is not available", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 unavailable?  Not a chance", e);
        }
    }


    // TODO Check this method below.

    /**
     * Метод для смены пароля пользователя
     * @param username логин пользователя
     * @param oldPassword старый пароль пользователя
     * @param newPassword новый пароль пользователя
     * @return буловое значение, установлен ли пароль или нет
     */
    public boolean changePassword(final String username, final String oldPassword, final String newPassword) {

        // Проверяем, действительно ли пользователь тот, за кого себя выдает
        // Заодно и пользователя вытащим
        Document user = validateLogin(username, oldPassword);

        // Если логин или пароль некорректен
        if (user == null) {
            System.out.println("Логин или пароль введен неверно!");
            return false;
        }

        String passwordHash = makePasswordHash(newPassword, Integer.toString(random.nextInt()));
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.append("$set", new BasicDBObject().append("password", passwordHash));

        BasicDBObject searchQuery = new BasicDBObject().append("username", username);
        usersCollection.updateOne(searchQuery, newDocument);

        return true;
    }
}
