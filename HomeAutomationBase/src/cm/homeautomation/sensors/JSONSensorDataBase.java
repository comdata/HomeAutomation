package cm.homeautomation.sensors;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonSubTypes({@Type(value=SensorDataRoomSaveRequest.class), @Type(value=SensorDataSaveRequest.class), @Type(value=WindowSensorData.class)})
@XmlRootElement
public abstract class JSONSensorDataBase {

}
