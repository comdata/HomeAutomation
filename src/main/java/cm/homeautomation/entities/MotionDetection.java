package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@AllArgsConstructor
public class MotionDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String externalId;
    private String type;
    private Date start;
    private Date end;
}