import de.a9d3.testing.checks.*;
import de.a9d3.testing.executer.SingleThreadExecutor;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import cm.homeautomation.entities.MQTTTopic;

public class MQTTTopicTest {
    @Test
    public void baseTest() {
        SingleThreadExecutor executor = new SingleThreadExecutor();

        assertTrue(executor.execute(MQTTTopic.class, Arrays.asList( 
                //new CopyConstructorCheck(), 
                //new DefensiveCopyingCheck(),
                //new EmptyCollectionCheck(), 
                new GetterIsSetterCheck(),
                //new HashcodeAndEqualsCheck(), 
                new PublicVariableCheck(true))));
    }
}