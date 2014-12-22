package pivotal.io;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import pivotal.io.RedisPublishSubscribeSourceForSpringXdApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(locations = {"redisPubSubTopicTest.xml"},classes=RedisPublishSubscribeSourceForSpringXdApplication.class)
public class RedisPublishSubscribeSourceForSpringXdApplicationTests {

	@Value("${redisTopicName}")
	private  String TOPIC_NAME;

	@Autowired
	@Qualifier("output")
	private DirectChannel outputChannel;
	
	@Autowired
	@Qualifier("extraOutput")
	private DirectChannel extraOutputChannel;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	private ArrayList<String> receivedList1 = new ArrayList<String>();
	private ArrayList<String> receivedList2 = new ArrayList<String>();

	@Before
	public void setup() throws InterruptedException {
		// topics are transient so once connections go away, topics are cleared automatically
		// create two subscribers
		outputChannel.subscribe(new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message)
					throws MessagingException {
				System.out.println("Received from output " + message.toString());
				receivedList1.add(message.getPayload().toString());
			}
		});
		extraOutputChannel.subscribe(new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message)
					throws MessagingException {
				System.out.println("Received from extraOutput " + message.toString());
				receivedList2.add(message.getPayload().toString());
			}
		});

	}

	@Test
	public void contextLoads() throws InterruptedException {
		// empty queue
		receivedList1.clear();receivedList2.clear();
		

		// populate
		for (int i = 0; i < 5; i++) {
			redisTemplate.convertAndSend(TOPIC_NAME, "JOEJOE"+i);
		}
		
		//wait for it all to be received
		while (receivedList1.size() != 5 && receivedList2.size() != 5) {
			Thread.sleep(1000);
		}
		
		
		//assert
		for (int i = 0 ; i < 5; i++) {
			String expectedPayload = "JOEJOE" + i;
			assertTrue("Channel 1 should contain "+expectedPayload,receivedList1.contains(expectedPayload));
			assertTrue("Channel 2 should contain "+expectedPayload,receivedList2.contains(expectedPayload));
		}
		receivedList1.clear();receivedList2.clear();
		
	}
}
