package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RemoteControlGroupMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String name;

	@ManyToOne
	@JoinColumn(name = "REMOTECONTROLGROUP_ID", nullable = false)
	RemoteControlGroup remoteControlGroup;

	@Enumerated(EnumType.STRING)
	RemoteControlGroupMemberType type;

	long externalId;
}
