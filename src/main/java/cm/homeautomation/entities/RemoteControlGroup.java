package cm.homeautomation.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	String name;

	@ManyToOne
	@JoinColumn(name = "REMOTE_ID", nullable = false)
	RemoteControl remote;

	@OneToMany(mappedBy = "remoteControlGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	List<RemoteControlGroupMember> members;
}
