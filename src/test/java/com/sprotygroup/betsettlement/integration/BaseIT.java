package com.sprotygroup.betsettlement.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ResourceLoader;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration"
})
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"event-outcomes", "settlement-tasks"})
@DirtiesContext
public class BaseIT {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ResourceLoader resourceLoader;

    protected String readMockRequest(final String path) {
        return readJsonFromFile("mock-request/" + path);
    }

    protected String readJsonFromFile(final String path) {
        try (Reader reader = new InputStreamReader(getResourceAsInputStream(path + ".json"), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getResourceAsInputStream(final String path) throws IOException {
        return resourceLoader.getResource(CLASSPATH_URL_PREFIX + path).getInputStream();
    }
}
