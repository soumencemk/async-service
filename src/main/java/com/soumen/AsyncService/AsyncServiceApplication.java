package com.soumen.AsyncService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.lang.System.currentTimeMillis;

@SpringBootApplication
@EnableAsync
public class AsyncServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsyncServiceApplication.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("GithubLookup-");
        executor.initialize();
        return executor;
    }

}

@Component
@Log4j2
@RequiredArgsConstructor
class AppRunner implements CommandLineRunner {
    private final GithubLookupService githubLookupService;

    @Override
    public void run(String... args) throws Exception {
        long start = currentTimeMillis();
        CompletableFuture<User> itssoumen = githubLookupService.findUser("itssoumen");
        CompletableFuture<User> jbloch = githubLookupService.findUser("jbloch");
        CompletableFuture<User> soumencemk = githubLookupService.findUser("soumencemk");
        CompletableFuture.allOf(itssoumen, jbloch, soumencemk).join();
        log.info("Elapesed time " + (currentTimeMillis() - start));
        log.info(itssoumen.get());
        log.info(jbloch.get());
        log.info(soumencemk.get());
    }
}

@Service
@Log4j2
class GithubLookupService {
    private final String GITHUB_USER = "https://api.github.com/users/%s";
    private final RestTemplate restTemplate;

    GithubLookupService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Async
    public CompletableFuture<User> findUser(String user) {
        log.info("Looking up - " + user);
        String url = String.format(GITHUB_USER, user);
        User userResponse = restTemplate.getForObject(url, User.class);
        return CompletableFuture.completedFuture(userResponse);
    }
}

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
class User {
    private String name;
    private String blog;
}