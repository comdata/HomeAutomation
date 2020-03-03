package cm.homeautomation.entities;

import java.util.List;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Sensor {

    @Getter
    @Setter    
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Getter
    @Setter    
	@Column(name = "SENSORNAME", nullable = false)
    private String sensorName;
    
    @Getter
    @Setter
    private String sensorType;
    
    @Getter
    @Setter    
    private String sensorPin;
    
    @Getter
    @Setter    
    private String sensorTechnicalType;
    
    @Getter
    @Setter    
    private String sensorPosition = "LOCAL";

    @Getter
    @Setter
	private int deadbandPercent = 0;
    
    @Getter
    @Setter    
	@Column(name = "MIN_VALUE", nullable = true)
    private String minValue;

    @Getter
    @Setter
	@Column(name = "MAX_VALUE", nullable = true)
	private String maxValue;

    @Getter
    @Setter    
	@Column(name = "SHOWDATA")
	private boolean showData;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@JsonBackReference("room")
	@ManyToOne
	@JoinColumn(name = "ROOM_ID")
	@EdmIgnore
	@JsonbTransient
	private Room room;

    @Getter
    @Setter    
    @OneToMany(mappedBy = "sensor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
	private List<SensorData> sensorData;

	@XmlTransient
	@JsonIgnore
	@JsonBackReference("room")
	public Room getRoom() {
		return room;
	}
	
	public void setRoom(Room room) {
		this.room = room;
	}
}
