package br.gov.servicos.to;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import static br.gov.model.util.Utilitarios.adicionarMeses;
import static br.gov.model.util.Utilitarios.adicionarDias;

public class ContaTO implements Serializable{
	private static final long serialVersionUID = -4290794295220149181L;
	
	private Integer idConta;
	private BigDecimal valorAgua;
	private BigDecimal valorEsgoto;
	private BigDecimal valorDebitos; 
	private BigDecimal valorCreditos;
	private Date dataRevisao;
	private Integer referencia;
	private Date dataVencimento; 
	private Short indicadorCobrancaMulta;
	private Short situacaoAtual;
	private Short digitoVerificador; 
	private Integer motivoRevisao;
	private Integer idImovel;
	private Integer consumoAgua;
	private BigDecimal valorImpostos;
	private Integer consumoEsgoto; 
	private BigDecimal valorPagamento; 
	private Date dataPagamento;
	private Integer idParcelamento;
	private Short diaVencimentoAlternativo;
	private Short indicadorVencimentoMesSeguinte = 2;
	private Date dataVencimentoConta;
	
	
	public Short getDiaVencimentoAlternativo() {
		return diaVencimentoAlternativo;
	}

	public void setDiaVencimentoAlternativo(Short dia) {
		this.diaVencimentoAlternativo = dia;
		Calendar cal = Calendar.getInstance();
		cal.setTime(dataVencimentoConta);
		cal.set(Calendar.DAY_OF_MONTH, dia);
		dataVencimentoConta = cal.getTime();
	}

	public Short getIndicadorVencimentoMesSeguinte() {
		return indicadorVencimentoMesSeguinte;
	}

	public void setIndicadorVencimentoMesSeguinte(Short indicadorVencimentoMesSeguinte) {
		this.indicadorVencimentoMesSeguinte = indicadorVencimentoMesSeguinte;
	}
	
	public Date getDataVencimentoConta() {
		return dataVencimentoConta;
	}

	public void setDataVencimentoConta(Date dataVencimentoConta) {
		this.dataVencimentoConta = dataVencimentoConta;
	}

	public boolean comVencimentoAlternativo(){
		return diaVencimentoAlternativo != null && diaVencimentoAlternativo.shortValue() > 0;
	}
	
	public boolean vencimentoMesSeguinte(){
		return indicadorVencimentoMesSeguinte != null && indicadorVencimentoMesSeguinte == 1;
	}

	public void adicionaMesAoVencimento() {
		dataVencimentoConta = adicionarMeses(dataVencimentoConta, (short) 1);
	}

	public void adicionaDiasAoVencimento(Short dias) {
		dataVencimentoConta = adicionarDias(dataVencimentoConta, dias);
	}
	
	public Integer getIdConta() {
		return idConta;
	}
	public void setIdConta(Integer idConta) {
		this.idConta = idConta;
	}
	public BigDecimal getValorAgua() {
		return valorAgua;
	}
	public void setValorAgua(BigDecimal valorAgua) {
		this.valorAgua = valorAgua;
	}
	public BigDecimal getValorEsgoto() {
		return valorEsgoto;
	}
	public void setValorEsgoto(BigDecimal valorEsgoto) {
		this.valorEsgoto = valorEsgoto;
	}
	public BigDecimal getValorDebitos() {
		return valorDebitos;
	}
	public void setValorDebitos(BigDecimal valorDebitos) {
		this.valorDebitos = valorDebitos;
	}
	public BigDecimal getValorCreditos() {
		return valorCreditos;
	}
	public void setValorCreditos(BigDecimal valorCreditos) {
		this.valorCreditos = valorCreditos;
	}
	public Date getDataRevisao() {
		return dataRevisao;
	}
	public void setDataRevisao(Date dataRevisao) {
		this.dataRevisao = dataRevisao;
	}
	public Integer getReferencia() {
		return referencia;
	}
	public void setReferencia(Integer referencia) {
		this.referencia = referencia;
	}
	public Date getDataVencimento() {
		return dataVencimento;
	}
	public void setDataVencimento(Date dataVencimento) {
		this.dataVencimento = dataVencimento;
	}
	public Short getIndicadorCobrancaMulta() {
		return indicadorCobrancaMulta;
	}
	public void setIndicadorCobrancaMulta(Short indicadorCobrancaMulta) {
		this.indicadorCobrancaMulta = indicadorCobrancaMulta;
	}
	public Short getSituacaoAtual() {
		return situacaoAtual;
	}
	public void setSituacaoAtual(Short situacaoAtual) {
		this.situacaoAtual = situacaoAtual;
	}
	public Short getDigitoVerificador() {
		return digitoVerificador;
	}
	public void setDigitoVerificador(Short digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}
	public Integer getMotivoRevisao() {
		return motivoRevisao;
	}
	public void setMotivoRevisao(Integer motivoRevisao) {
		this.motivoRevisao = motivoRevisao;
	}
	public Integer getIdImovel() {
		return idImovel;
	}
	public void setIdImovel(Integer idImovel) {
		this.idImovel = idImovel;
	}
	public Integer getConsumoAgua() {
		return consumoAgua;
	}
	public void setConsumoAgua(Integer consumoAgua) {
		this.consumoAgua = consumoAgua;
	}
	public BigDecimal getValorImpostos() {
		return valorImpostos;
	}
	public void setValorImpostos(BigDecimal valorImpostos) {
		this.valorImpostos = valorImpostos;
	}
	public Integer getConsumoEsgoto() {
		return consumoEsgoto;
	}
	public void setConsumoEsgoto(Integer consumoEsgoto) {
		this.consumoEsgoto = consumoEsgoto;
	}
	public BigDecimal getValorPagamento() {
		return valorPagamento;
	}
	public void setValorPagamento(BigDecimal valorPagamento) {
		this.valorPagamento = valorPagamento;
	}
	public Date getDataPagamento() {
		return dataPagamento;
	}
	public void setDataPagamento(Date dataPagamento) {
		this.dataPagamento = dataPagamento;
	}
	public Integer getIdParcelamento() {
		return idParcelamento;
	}
	public void setIdParcelamento(Integer idParcelamento) {
		this.idParcelamento = idParcelamento;
	}

}
