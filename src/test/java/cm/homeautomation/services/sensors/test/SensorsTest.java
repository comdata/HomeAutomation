package cm.homeautomation.services.sensors.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import cm.homeautomation.sensors.SensorDatas;
import cm.homeautomation.services.sensors.Sensors;

class SensorsTest {
    
    @Test
    void testGetInstance() {
        Sensors instance = Sensors.getInstance();

        assertNotNull(instance);
    }

    @Test
    void testGetDataForRoomNotExisting() {
        Sensors instance = Sensors.getInstance();
       
        assertNotNull(instance);

        SensorDatas dataForRoom = instance.getDataForRoom("1");

        assertTrue(dataForRoom.getSensorData().size()==0);
    }
}