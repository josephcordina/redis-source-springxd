package demo;

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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(locations = {"redisQueueTest.xml"},classes=RedisQueueSourceForSpringXdApplication.class)
public class RedisQueueSourceForSpringXdApplicationTests {

	@Value("${redisQueueName}")
	private  String QUEUE_NAME;

	@Autowired
	@Qualifier("output")
	private DirectChannel outputChannel;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	private ArrayList<String> receivedList = new ArrayList<String>();

	@Before
	public void setup() throws InterruptedException {
		outputChannel.subscribe(new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message)
					throws MessagingException {
				//System.out.println("Received " + message.toString());
				receivedList.add(message.getPayload().toString());
			}
		});
		// lets sleep a bit to allow messages to be cleared out from queue
		Thread.sleep(300);
	}

	@Test
	public void contextLoads() throws InterruptedException {
		// empty queue
		receivedList.clear();
		redisTemplate.boundListOps(QUEUE_NAME).trim(1, 0);
		
		// populate
		for (int i = 0; i < 5; i++) {
			redisTemplate.boundListOps(QUEUE_NAME).rightPush("JOEJOE"+i);
		}
		
		//wait for it all to be received
		while (receivedList.size() != 5) {
			Thread.sleep(1000);
		}
		
		//assert
		for (int i = 0 ; i < 5; i++) {
			String expectedPayload = "JOEJOE" + i;
			assertTrue("Should contain "+expectedPayload,receivedList.contains(expectedPayload));
		}
		receivedList.clear();
		redisTemplate.boundListOps(QUEUE_NAME).trim(1, 0);
	}
}
