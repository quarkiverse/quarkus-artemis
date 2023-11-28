package io.quarkus.it.artemis.ra;

import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Gift extends PanacheEntity {

    public String name;
}
