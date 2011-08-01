package org.alveolo.cocoon.springification;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;


@NamedQuery(name = "findRecordById", query = "SELECT r FROM Record r WHERE r.id = :id")

@Entity
@Table(name = "RECORD")
@XmlRootElement
public class Record {
	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Integer id;

	@Column(name = "NAME")
	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	public Record() {}

	public Record(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
