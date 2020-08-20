package cm.homeautomation.services.mqtt.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import cm.homeautomation.entities.MQTTTopic;
import cm.homeautomation.services.mqtt.MQTTTopicService;
import de.a9d3.testing.checks.GetterIsSetterCheck;
import de.a9d3.testing.checks.PublicVariableCheck;
import de.a9d3.testing.executer.SingleThreadExecutor;

class MQTTTopicServiceTest {
    
    @Test
    void baseTest() {
        SingleThreadExecutor executor = new SingleThreadExecutor();

        assertTrue(executor.execute(MQTTTopicService.class, Arrays.asList( 
                //new CopyConstructorCheck(), 
                //new DefensiveCopyingCheck(),
                //new EmptyCollectionCheck(), 
                new GetterIsSetterCheck(),
                //new HashcodeAndEqualsCheck(), 
                new PublicVariableCheck(true))));
    }

    @Test
    void testGetAll() {
        List<MQTTTopic> all = new MQTTTopicService().getAll();
        assertTrue(all!=null);
        assertTrue(all.isEmpty());
    }

}