package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "HEARING_DAY")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@Getter
@With
@EqualsAndHashCode(callSuper = true, exclude = "court")
@Audited
public class HearingDayEntity extends BaseAuditedEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_HEARING_ID", referencedColumnName = "id", nullable = false)
    @Setter
    private HearingEntity hearing;

    @Column(name = "HEARING_DAY", nullable = false)
    private final LocalDate day;

    @Column(name = "HEARING_TIME", nullable = false)
    private final LocalTime time;

    @Column(name = "COURT_CODE", nullable = false)
    private final String courtCode;

    @NotAudited
    @OneToOne
    @JoinColumn(name = "COURT_CODE", referencedColumnName = "COURT_CODE", insertable = false, updatable = false)
    private final CourtEntity court;

    @Column(name = "COURT_ROOM", nullable = false)
    private final String courtRoom;

    public CourtSession getSession() {
        return CourtSession.from(time);
    }

    public LocalDateTime getSessionStartTime() {
        return LocalDateTime.of(day, time);
    }

    public String loggableString(){
        return String.format("%s|%s|%sT%s", courtCode, courtRoom, day, time);
    }
}
