package cm.homeautomation.sensors;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include=JsonTypeInfo.As.PROPERTY, property="@c")
@JsonSubTypes({@Type(value=SensorDataRoomSaveRequest.class), @Type(value=SensorDataSaveRequest.class), @Type(value=WindowSensorData.class)})
@JsonInclude (JsonInclude.Include.NON_EMPTY)
@XmlRootElement
public abstract class JSONSensorDataBase {

}
