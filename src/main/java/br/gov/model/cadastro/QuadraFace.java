package br.gov.model.cadastro;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="quadra_face", schema="cadastro")
public class QuadraFace implements Serializable{
	private static final long serialVersionUID = 465351043576743729L;

	@Id
	@Column(name="qdfa_id")
	private Integer id;
	
	@Column(name="qdfa_nnfacequadra")
	private Integer numeroQuadraFace;
	
	public QuadraFace() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getNumeroQuadraFace() {
		return numeroQuadraFace;
	}

	public void setNumeroQuadraFace(Integer numeroQuadraFace) {
		this.numeroQuadraFace = numeroQuadraFace;
	}

	public String toString() {
		return "QuadraFace [id=" + id + ", numeroQuadraFace=" + numeroQuadraFace + "]";
	}
}