package cm.homeautomation.entities;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RemoteControlGroup {
	@Id
	String id;

	String name;

	RemoteControl remote;

	@OneToMany
	List<RemoteControlGroupMember> members;
}
