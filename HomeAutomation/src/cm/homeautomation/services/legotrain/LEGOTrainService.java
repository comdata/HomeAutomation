package cm.homeautomation.services.legotrain;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import cm.homeautomation.services.base.BaseService;

/**
 * name of the class is just here for describing what it does and not to have any claim on that name
 * 
 * //SinglePin & SingleOutput modes
#define CONTINUOUS 0x2
#define TIMEOUT 0x3
#define PWM 0x0
#define CST 0x2 //Clear - Set - Toggle

//ComboPWM & SingleOutput pwm steps
#define PWM_FLT 0x0
#define PWM_FWD1 0x1
#define PWM_FWD2 0x2
#define PWM_FWD3 0x3
#define PWM_FWD4 0x4
#define PWM_FWD5 0x5
#define PWM_FWD6 0x6
#define PWM_FWD7 0x7
#define PWM_BRK 0x8
#define PWM_REV7 0x9
#define PWM_REV6 0xA
#define PWM_REV5 0xB
#define PWM_REV4 0xC
#define PWM_REV3 0xD
#define PWM_REV2 0xE
#define PWM_REV1 0xf

//SingleOutput Clear - Set - Toggle steps
#define CL_C1_CL_C2 0x0
#define ST_C1_CL_C2 0x1
#define CL_C1_ST_C2 0x2
#define ST_C1_ST_C2 0x3
#define INC_PWM 0x4
#define DEC_PWM 0x5
#define FULL_FWD 0x6
#define FULL_REV 0x7
#define TOG_FWD_REV 0x8

//ComboMode steps
#define RED_FLT 0x0
#define RED_FWD 0x1
#define RED_REV 0x2
#define RED_BRK 0x3
#define BLUE_FLT 0x0
#define BLUE_FWD 0x4
#define BLUE_REV 0x8
#define BLUE_BRK 0xC

//channels
#define CH1 0x0
#define CH2 0x1
#define CH3 0x2
#define CH4 0x3

//SinglePin & SingleOutput output
#define RED 0x0
#define BLUE 0x1

//SinglePin functions
#define NO_CHANGE 0x0
#define CLEAR 0x1
#define SET 0x2
#define TOGGLE 0x3

//SinglePin pin
#define PIN_C1 0x0
#define PIN_C2 0x1
 * 
 * @author christoph
 *
 */
@Path("lego")
public class LEGOTrainService extends BaseService {

	@GET
	@Path("control/{train}/{speed}/{light}")
	public void controlTrain(@PathParam("train") String train, @PathParam("speed") int speed, @PathParam("light") int light) {
		//{mode: 0, step: 0, output: 0, channel:0}
		
		String jsonMessage="{mode: 0, step: "+speed+", output: 0, channel:"+train+"}";
		sendMQTTMessage(jsonMessage);
		
		jsonMessage="{mode: 0, step: "+light+", output: 1, channel:"+train+"}";
		sendMQTTMessage(jsonMessage);
	}
	
	@GET
	@Path("emergencyStop")
	public void stopAllTrains() {
		
		for(int i=0;i<4;i++) { 
		
			for(int a=0;a<2;a++) {
				String jsonMessage="{mode: 0, step: 8, output: "+a+", channel:"+i+"}";
				sendMQTTMessage(jsonMessage);
			}
		}
	}
	
	private void sendMQTTMessage(String jsonMessage) {
		try {
			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();

			MqttClient client;

			client = new MqttClient("tcp://localhost:1883", "HomeAutomation/" + randomUUIDString);

			MqttConnectOptions connOpt = new MqttConnectOptions();
			connOpt.setAutomaticReconnect(true);
			connOpt.setCleanSession(true);
			connOpt.setKeepAliveInterval(60);
			connOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
			// connOpt.setUserName(M2MIO_USERNAME);
			// connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());

			client.connect(connOpt);

			MqttMessage message = new MqttMessage();
			message.setPayload(jsonMessage.getBytes());
			client.publish("/lego", message);
			client.disconnect();
			client.close();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
