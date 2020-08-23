package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class ManualTask {

	@Id
	private Long id;

	@NonNull
	private String name;
	@NonNull
	private String text;
	@NonNull
	private String type;
	private Long externalId;

	private Date createdDateTime;
	private Date closedDateTime;

	private boolean done = false;
}
