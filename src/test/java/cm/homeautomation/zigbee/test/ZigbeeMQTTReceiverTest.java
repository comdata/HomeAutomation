package cm.homeautomation.zigbee.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.zigbee.ZigbeeMQTTReceiver;

class ZigbeeMQTTReceiverTest {

   // @BeforeEach
    public void setup() {
        ConfigurationService.createOrUpdate("zigbee", "mqttTopic", "zigbee2mqtt");
    }

    //@Test
    void testReceiveEvent() throws JsonMappingException, JsonProcessingException {
   		ZigbeeMQTTReceiver zigbeeMQTTReceiver = new ZigbeeMQTTReceiver();

		MQTTTopicEvent zigbeeDeviceEvent = new MQTTTopicEvent();

		zigbeeDeviceEvent.setTopic("zigbee2mqtt" + "/bridge/config/devices");

        String ieeeAddr="0x99111b0018e27f02";

        zigbeeDeviceEvent.setMessage(
				"[{\"ieeeAddr\":\""+ieeeAddr+"\",\"type\":\"Coordinator\",\"networkAddress\":0,\"friendly_name\":\"Coordinator\",\"softwareBuildID\":\"zStack12\",\"dateCode\":\"20190608\",\"lastSeen\":1587295440005},{\"ieeeAddr\":\"0x00158d000451907f\",\"type\":\"EndDevice\",\"networkAddress\":44744,\"model\":\"RTCGQ11LM\",\"vendor\":\"Xiaomi\",\"description\":\"Aqara human body movement and illuminance sensor\",\"friendly_name\":\"0x00158d000451907f\",\"manufacturerID\":4151,\"manufacturerName\":\"LUMI\",\"powerSource\":\"Battery\",\"modelID\":\"lumi.sensor_motion.aq2\",\"lastSeen\":1587294981305},{\"ieeeAddr\":\"0x000b57fffe521310\",\"type\":\"EndDevice\",\"networkAddress\":9892,\"model\":\"E1524/E1810\",\"vendor\":\"IKEA\",\"description\":\"TRADFRI remote control\",\"friendly_name\":\"0x000b57fffe521310\",\"manufacturerID\":4476,\"manufacturerName\":\"IKEA of Sweden\",\"powerSource\":\"Battery\",\"modelID\":\"TRADFRI remote control\",\"hardwareVersion\":1,\"softwareBuildID\":\"2.3.014\",\"dateCode\":\"20190708\",\"lastSeen\":1587292304389},{\"ieeeAddr\":\"0xec1bbdfffe85e485\",\"type\":\"Router\",\"networkAddress\":673,\"model\":\"ICPSHC24-10EU-IL-1\",\"vendor\":\"IKEA\",\"description\":\"TRADFRI driver for wireless control (10 watt)\",\"friendly_name\":\"Arbeitsleuchte\",\"manufacturerID\":4476,\"manufacturerName\":\"IKEA of Sweden\",\"powerSource\":\"Mains (single phase)\",\"modelID\":\"TRADFRI Driver 10W\",\"hardwareVersion\":1,\"softwareBuildID\":\"1.2.245\",\"dateCode\":\"20170529\",\"lastSeen\":1587293860058}]");

		zigbeeMQTTReceiver.receiveMQTTTopicEvents(zigbeeDeviceEvent);
    }

   // @Test
    void testReceiveEventBridgeOnly() throws JsonMappingException, JsonProcessingException {
   		ZigbeeMQTTReceiver zigbeeMQTTReceiver = new ZigbeeMQTTReceiver();

		MQTTTopicEvent zigbeeDeviceEvent = new MQTTTopicEvent();

		zigbeeDeviceEvent.setTopic("zigbee2mqtt" + "/bridge/1234");

        zigbeeDeviceEvent.setMessage("");

		zigbeeMQTTReceiver.receiveMQTTTopicEvents(zigbeeDeviceEvent);
    }

    //@Test
    void testReceiveEventTradfriDriver() throws JsonMappingException, JsonProcessingException {
        ZigbeeMQTTReceiver zigbeeMQTTReceiver = new ZigbeeMQTTReceiver();

        MQTTTopicEvent zigbeeDeviceEvent = new MQTTTopicEvent();

        zigbeeDeviceEvent.setTopic("zigbee2mqtt" + "/Arbeitsleuchte");

        String ieeeAddr = "0x99111b0018e21111";

        zigbeeDeviceEvent.setMessage("[{\"ieeeAddr\":\"" + ieeeAddr
                + "\",\"type\":\"Router\",\"networkAddress\":673,\"model\":\"ICPSHC24-10EU-IL-1\",\"vendor\":\"IKEA\",\"description\":\"TRADFRI driver for wireless control (10 watt)\",\"friendly_name\":\"Arbeitsleuchte\",\"manufacturerID\":4476,\"manufacturerName\":\"IKEA of Sweden\",\"powerSource\":\"Mains (single phase)\",\"modelID\":\"TRADFRI Driver 10W\",\"hardwareVersion\":1,\"softwareBuildID\":\"1.2.245\",\"dateCode\":\"20170529\",\"lastSeen\":1587293860058}]");

        zigbeeMQTTReceiver.receiveMQTTTopicEvents(zigbeeDeviceEvent);

    }
}