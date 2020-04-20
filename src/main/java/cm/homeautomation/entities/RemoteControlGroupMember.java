package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
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
	String id;

	String name;

	@ManyToOne
	RemoteControlGroup remoteControlGroup;

	@Enumerated(EnumType.STRING)
	RemoteControlGroupMemberType type;

	long externalId;
}
