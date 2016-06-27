package cm.homeautomation.sensors;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@JsonSubTypes({@Type(value=SensorDataRoomSaveRequest.class), @Type(value=SensorDataSaveRequest.class)})
@XmlRootElement
public class JSONSensorDataBase {

}
