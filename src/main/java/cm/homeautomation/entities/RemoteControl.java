package cm.homeautomation.entities;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RemoteControl {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String name;
	String technicalId;

	@OneToMany(mappedBy = "remote")
	List<RemoteControlGroup> groups;
	
	public enum RemoteType {
		ZIGBEE, DASHBUTTON, HUE
	}

	@NonNull
	@Enumerated(EnumType.STRING)
	RemoteType remoteType;
}
