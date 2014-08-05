package br.gov.model.faturamento;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import br.gov.model.Status;

@Entity
@Table(name="fatur_situacao_tipo", schema="faturamento")
public class FaturamentoSituacaoTipo {
	
	@Id
	@Column(name="ftst_id")
	private Long id;
	
    @Column(name="ftst_icfaturamentoparalisacao")
	private Short paralisacaoFaturamento;

    @Column(name="ftst_icvalidoagua")
    private Short validoAgua;
    
    public FaturamentoSituacaoTipo() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Short getParalisacaoFaturamento() {
		return paralisacaoFaturamento;
	}

	public void setParalisacaoFaturamento(Short paralisacaoFaturamento) {
		this.paralisacaoFaturamento = paralisacaoFaturamento;
	}
	
	public Short getValidoAgua() {
		return validoAgua;
	}

	public void setValidoAgua(Short validoAgua) {
		this.validoAgua = validoAgua;
	}

	public String toString() {
		return "FaturamentoSituacaoTipo [id=" + id + ", paralisacaoFaturamento=" + paralisacaoFaturamento + ", validoAgua="	+ validoAgua + "]";
	}
}
