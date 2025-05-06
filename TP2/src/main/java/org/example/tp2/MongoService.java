package org.example.tp2;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoService {
    public static void main(String[] args) {
        String connectionString = "mongodb+srv://onlymd:onlymd2003@cluster0.atystss.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("admin");
                database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
    public void insertUser(User user) {
        MongoDatabase database = getDatabase(); // Get the database instance
        var collection = database.getCollection("users"); // Replace "users" with your collection name
        Document doc = new Document("name", user.getName())
                .append("email", user.getEmail());
        collection.insertOne(doc);
    }

    private MongoDatabase getDatabase() {
        String connectionString = "mongodb+srv://onlymd:onlymd2003@cluster0.atystss.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        return mongoClient.getDatabase("yourDatabaseName"); // Replace "yourDatabaseName" with your database name
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        var collection = getDatabase().getCollection("users"); // Replace "users" with your collection name
        for (Document doc : collection.find()) {
            users.add(new User(doc.getString("name"), doc.getString("email")));
        }
        return users;
    }
    
}

