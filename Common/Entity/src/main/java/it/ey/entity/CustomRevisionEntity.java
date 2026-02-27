package it.ey.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.RevisionEntity;
import jakarta.persistence.*;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;


@Entity
@RevisionEntity
@Table(name = "revinfo")
public class CustomRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rev_id_generator")
    @SequenceGenerator(name = "rev_id_generator", sequenceName = "revinfo_seq", allocationSize = 1)
    @RevisionNumber
    @Column(name = "id")
    private int id;

    @RevisionTimestamp
    @Column(name = "timestamp")
    private long timestamp;
}


