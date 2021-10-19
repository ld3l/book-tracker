package com.my.booktrack;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.session.Session;
import com.my.booktrack.connection.DataStaxAstraConfig;
import com.my.booktrack.domain.entity.Author;
import com.my.booktrack.repository.AuthorRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.core.AsyncCassandraTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraConfig.class)
public class BookTrackApplication {

    @Autowired
    AuthorRepository repository;

    @Autowired
    AsyncCassandraTemplate template;

    public static void main(String[] args) {
        SpringApplication.run(BookTrackApplication.class, args);
    }


    private void initAuthors() {
        try (Stream<String> lines = Files.lines(Paths.get("test-authors.txt"))) {
            lines.forEach(line -> {
                String json = line.substring(line.indexOf('{'));

                try {
                    JSONObject object = new JSONObject(json);
                    Author author = new Author();
                    author.setPseudonym(object.optString("name"));
                    author.setName(object.optString("personalName"));
                    author.setId(object.optString("key").replace("/authors/", ""));
                    System.out.println("save author " + author.getName());
                    template.insert(author);//TODO нужно ограничить до 1024, а иначе может упасть
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initWorks() {
    }

    @PostConstruct
    public void onStart() {
       initAuthors();
       initWorks();
    }

    @Bean
    AsyncCassandraTemplate asyncCassandraTemplate(CqlSession session) {
        return new AsyncCassandraTemplate(session);
    }

    @Bean
    public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraConfig astraConfig) {
        Path bundle = astraConfig.getSecureConnectBundle().toPath();
        return builder -> builder.withCloudSecureConnectBundle(bundle);
    }
}
