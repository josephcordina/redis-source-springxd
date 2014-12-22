package demo;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("redisPubSubtopic/config/redisPubSubTopic.xml")
@EnableAutoConfiguration
public class RedisPublishSubscribeSourceForSpringXdApplication {

}
