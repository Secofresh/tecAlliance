package org.interview.tecalliance;

import org.interview.tecalliance.config.TestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class TecAllianceApplicationTests {

    @Test
    void contextLoads() {}

}
