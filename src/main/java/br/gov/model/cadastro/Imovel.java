package br.gov.model.cadastro;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAgua;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgoto;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.endereco.EnderecoReferencia;
import br.gov.model.cadastro.endereco.LogradouroCep;
import br.gov.model.faturamento.ConsumoTarifa;
import br.gov.model.faturamento.FaturamentoSituacaoTipo;
import br.gov.model.micromedicao.HidrometroInstalacaoHistorico;
import br.gov.model.micromedicao.Rota;
import br.gov.model.util.Utilitarios;

@Entity
@Table(name="imovel", schema="cadastro")
public class Imovel implements Serializable{
	private static final long serialVersionUID = 1200767585478407463L;

	public final static Short IMOVEL_EXCLUIDO = Short.valueOf("1");

	@Id
	@Column(name="imov_id")
	private Integer id;

	@Column(name="imov_nnimovel", columnDefinition="bpchar(5)")
	private String numeroImovel;
	
	@Column(name="imov_nnsequencialrota")
	private Integer numeroSequencialRota;
	
	@Column(name="imov_nnlote")
	private Short lote;
	
	@Column(name="imov_nnsublote")
	private Short subLote;
	
	@Column(name="imov_ddvencimento")
	private Short diaVencimento;
	
	@Column(name="imov_icemsextfatmt")
	private Short indicadorEmissaoExtratoFaturamento;
	
	@Column(name="imov_icexclusao")
	private Short indicadorExclusao;
	
	@Column(name="imov_icimovelcondominio")
	private Short indicadorImovelCondominio;
	
	@Column(name="imov_icvencimentomesseguinte")
	private Short indicadorVencimentoMesSeguinte;
	
	@Column(name="icte_id")
	private Integer imovelContaEnvio;
	
	@Column(name="imov_icdebitoconta")
	private Short indicadorDebitoConta;
	
	@Column(name="poco_id")
	private Integer pocoTipo;
	
	@Column(name="imov_dscomplementoendereco")
	private String complementoEndereco;
	
	@Column(name="imov_nnareaconstruida")
	private BigDecimal areaConstruida;
	
	@ManyToOne
	@JoinColumn(name="loca_id")
	private Localidade localidade;
	
	@ManyToOne
	@JoinColumn(name="stcm_id")
	private SetorComercial setorComercial;

	@ManyToOne
	@JoinColumn(name="qdra_id")
	private Quadra quadra;

	@ManyToOne
	@JoinColumn(name="qdfa_id")
	private QuadraFace quadraFace;
	
	@OneToOne(mappedBy="imovel")
	private LigacaoAgua ligacaoAgua;
	
	@OneToOne(mappedBy="imovel")
	private LigacaoEsgoto ligacaoEsgoto;
	
	@ManyToOne
	@JoinColumn(name="last_id")
	private LigacaoAguaSituacao ligacaoAguaSituacao;
	
	@ManyToOne
	@JoinColumn(name="lest_id")
	private LigacaoEsgotoSituacao ligacaoEsgotoSituacao;
	
	@ManyToOne
	@JoinColumn(name="ftst_id")
	private FaturamentoSituacaoTipo faturamentoSituacaoTipo;

	@ManyToOne
	@JoinColumn(name="imov_idimovelcondominio")
	private Imovel imovelCondominio;
		
	@ManyToOne
	@JoinColumn(name="hidi_id")
	private HidrometroInstalacaoHistorico hidrometroInstalacaoHistorico;
	
	@ManyToOne
	@JoinColumn(name="rota_idalternativa", referencedColumnName="rota_id")
	private Rota rotaAlternativa;

	@ManyToOne
	@JoinColumn(name="iper_id")
	private ImovelPerfil imovelPerfil;
	
	@OneToMany(mappedBy="imovel")
	private List<ClienteImovel> clienteImoveis;

	@ManyToOne
	@JoinColumn(name="lgbr_id")
	private LogradouroBairro logradouroBairro;
	
	@ManyToOne
	@JoinColumn(name="lgcp_id")
	private LogradouroCep logradouroCep;
	
	@ManyToOne
	@JoinColumn(name="edrf_id")
	private EnderecoReferencia enderecoReferencia;
	
	@ManyToOne
	@JoinColumn(name="logr_idinicioperimetro", referencedColumnName="logr_id")
	private Logradouro perimetroInicial;
	
	@ManyToOne
	@JoinColumn(name="logr_idfimperimetro", referencedColumnName="logr_id")
	private Logradouro perimetroFinal;
	
	@ManyToOne
	@JoinColumn(name="cstf_id")
	private ConsumoTarifa consumoTarifa;
	
	@ManyToOne
	@JoinColumn(name="acon_id")
	private AreaConstruidaFaixa areaConstruidaFaixa;
	
	public Imovel() {}
	
	public Imovel(Integer id){
		this.id  = id;
	}

	public boolean responsavelRecebeConta() {
		return imovelContaEnvio != null &&
		       (imovelContaEnvio == ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL.getId() 
		 	 || imovelContaEnvio == ImovelContaEnvio.NAO_PAGAVEL_IMOVEL_PAGAVEL_RESPONSAVEL.getId()
		 	   );
	}

	public boolean debitoEmConta() {
		return indicadorDebitoConta != null && indicadorDebitoConta == Status.ATIVO.getId();
	}

	public  boolean aguaEsgotoLigados() {
		return ligacaoAguaSituacao != null
				&& ligacaoAguaSituacao.getId().equals(LigacaoAguaSituacao.LIGADO)
				&& ligacaoEsgotoSituacao != null
				&& ligacaoEsgotoSituacao.getId().equals(LigacaoEsgotoSituacao.LIGADO);
	}		

	public boolean aguaLigada() {
		return ligacaoAguaSituacao.getId().equals(LigacaoAguaSituacao.LIGADO);
	}

	public boolean esgotoLigado() {
		return ligacaoEsgotoSituacao.getId().equals(LigacaoEsgotoSituacao.LIGADO);
	}

	public boolean pertenceACondominio() {
		return imovelCondominio != null;
	}
	
	public boolean ehCondominio() {
		return indicadorImovelCondominio != null && indicadorImovelCondominio == Status.ATIVO.getId();
	}
	
	public boolean faturamentoEsgotoAtivo(){
	    return ligacaoEsgotoSituacao != null && ligacaoEsgotoSituacao.getSituacaoFaturamento() != null
	            && ligacaoEsgotoSituacao.getSituacaoFaturamento().shortValue() == Status.ATIVO.getId();
	}

	public boolean faturamentoAguaAtivo(){
	    return ligacaoAguaSituacao != null && ligacaoAguaSituacao.getSituacaoFaturamento() != null
	            && ligacaoAguaSituacao.getSituacaoFaturamento().shortValue() == Status.ATIVO.getId();
	}
	
	public boolean paralisacaoFaturamento() {
		return faturamentoSituacaoTipo != null && faturamentoSituacaoTipo.getParalisacaoFaturamento() == Status.ATIVO.getId();
	}

	public boolean faturamentoAguaValido() {
		return faturamentoSituacaoTipo != null &&  faturamentoSituacaoTipo.getValidoAgua() == Status.ATIVO.getId();
	}

	public boolean existeHidrometro(){
		return existeHidrometroAgua() || existeHidrometroPoco();
	}
	
	public boolean existeHidrometroAgua(){
		return ligacaoAgua != null && ligacaoAgua.getHidrometroInstalacoesHistorico() != null && ligacaoAgua.getHidrometroInstalacoesHistorico().size() > 0;
	}

	public boolean existeHidrometroPoco(){
		return hidrometroInstalacaoHistorico != null;
	}
	
	public boolean possuiLigacaoAguaAtiva() {
		return this.getLigacaoAguaSituacao().getSituacaoFaturamento().equals(Status.ATIVO);
	}
	
	public boolean possuiLigacaoEsgotoAtiva() {
		return this.getLigacaoEsgotoSituacao().getSituacaoFaturamento().equals(Status.ATIVO);
	}
		
	public String getInscricaoFormatadaSemPonto() {
		StringBuilder inscricao = new StringBuilder();
		inscricao.append(Utilitarios.completaComZerosEsquerda(3, localidade.getId()))
			.append(Utilitarios.completaComZerosEsquerda(3, setorComercial.getId()))
			.append(Utilitarios.completaComZerosEsquerda(3, quadra.getNumeroQuadra()))
			.append(Utilitarios.completaComZerosEsquerda(4, lote))
			.append(Utilitarios.completaComZerosEsquerda(3, subLote));
		
		return inscricao.toString();
	}
	
	public boolean enviarContaParaImovel() {
		return imovelContaEnvio != null && imovelContaEnvio == ImovelContaEnvio.ENVIAR_IMOVEL.getId();
	}

	public boolean enviarContaParaResponsavel() {
		return imovelContaEnvio != null && imovelContaEnvio == ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL_FINAL_GRUPO.getId();
	}
	
	public Integer consumoMinimoAgua(){
		return ligacaoAgua != null ? ligacaoAgua.getConsumoMinimoAgua() : null;
	}
	
	public Integer consumoMinimoEsgoto(){
		return ligacaoEsgoto != null ? ligacaoEsgoto.getConsumoMinimo() : null;
	}
	
	public BigDecimal percentualAguaConsumidaColetada(){
		return ligacaoEsgoto != null ? ligacaoEsgoto.getPercentualAguaConsumidaColetada() : null;
	}
	
	public BigDecimal percentualEsgoto(){
		return possuiLigacaoEsgotoAtiva() ? ligacaoEsgoto.getPercentual() : BigDecimal.ZERO; 
	}
	
	public Integer tarifaTipoCalculo() {
		return consumoTarifa != null ? consumoTarifa.getTarifaTipoCalculo() : null;
	}
	
	public boolean existeDiaVencimento(){
		return diaVencimento != null && diaVencimento.intValue() != 0;
	}
	
	public boolean emissaoExtratoFaturamento(){
		return indicadorEmissaoExtratoFaturamento != null && indicadorEmissaoExtratoFaturamento == (short) 1;
	}

	public StringBuilder getEnderecoFormatadoAbreviado() {
		StringBuilder endereco = new StringBuilder();

		if (logradouroCep != null && logradouroCep.getLogradouro() != null) {

			if (logradouroCep.getLogradouro().getLogradouroTipo() != null) {
				if (logradouroCep.getLogradouro().getLogradouroTipo().getDescricaoAbreviada() != null) {
					endereco.append(logradouroCep.getLogradouro().getLogradouroTipo().getDescricaoAbreviada().trim());
				}
			}
			if (logradouroCep.getLogradouro().getLogradouroTitulo() != null) {
				if (logradouroCep.getLogradouro().getLogradouroTitulo().getDescricaoAbreviada() != null) {
					endereco.append(" ").append(logradouroCep.getLogradouro().getLogradouroTitulo().getDescricaoAbreviada().trim());
				}
			}

			endereco.append(" ").append(logradouroCep.getLogradouro().getNome().trim());

			if (enderecoReferencia != null) {
				if (enderecoReferencia.getDescricaoAbreviada() != null) {
					endereco.append(", ").append(enderecoReferencia.getDescricaoAbreviada().trim());
				}
			}

			endereco.append(numeroImovel != null ? numeroImovel.trim(): "");

			if (complementoEndereco != null) {
				endereco.append(" - ").append(complementoEndereco.trim());
			}

			if (logradouroBairro != null && logradouroBairro.getBairro() != null) {
				endereco.append(" - ").append(logradouroBairro.getBairro().getNome().trim());

				if (logradouroBairro.getBairro().getMunicipio() != null) {
					endereco.append(" ").append(logradouroBairro.getBairro().getMunicipio().getNome().trim());
					
					if (logradouroBairro.getBairro().getMunicipio().getUnidadeFederacao() != null) {
						endereco.append(" ").append(logradouroBairro.getBairro().getMunicipio().getUnidadeFederacao().getSigla().trim());
					}
				}

			}

			if (logradouroCep.getCep() != null) {
				endereco.append(" ").append(logradouroCep.getCep().getCepFormatado().trim());
			}

			if (perimetroInicial != null) {
				endereco.append(" ENTRE ").append(perimetroInicial.getDescricaoFormatada())
				.append(" E ").append(perimetroFinal.getDescricaoFormatada());
			}

		}

		return endereco;
	}

	/**********************************************
	 ************ GETTERS AND SETTERS ************* 
	 **********************************************/
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Localidade getLocalidade() {
		return localidade;
	}

	public void setLocalidade(Localidade localidade) {
		this.localidade = localidade;
	}

	public SetorComercial getSetorComercial() {
		return setorComercial;
	}

	public void setSetorComercial(SetorComercial setorComercial) {
		this.setorComercial = setorComercial;
	}

	public Quadra getQuadra() {
		return quadra;
	}

	public void setQuadra(Quadra quadra) {
		this.quadra = quadra;
	}

	public QuadraFace getQuadraFace() {
		return quadraFace;
	}

	public void setQuadraFace(QuadraFace quadraFace) {
		this.quadraFace = quadraFace;
	}

	public String getNumeroImovel() {
		return numeroImovel;
	}

	public void setNumeroImovel(String numeroImovel) {
		this.numeroImovel = numeroImovel;
	}

	public Integer getNumeroSequencialRota() {
		return numeroSequencialRota;
	}

	public void setNumeroSequencialRota(Integer numeroSequencialRota) {
		this.numeroSequencialRota = numeroSequencialRota;
	}

	public LigacaoAgua getLigacaoAgua() {
		return ligacaoAgua;
	}

	public void setLigacaoAgua(LigacaoAgua ligacaoAgua) {
		this.ligacaoAgua = ligacaoAgua;
	}
	
	public LigacaoEsgoto getLigacaoEsgoto() {
		return ligacaoEsgoto;
	}

	public void setLigacaoEsgoto(LigacaoEsgoto ligacaoEsgoto) {
		this.ligacaoEsgoto = ligacaoEsgoto;
	}

	public LigacaoAguaSituacao getLigacaoAguaSituacao() {
		return ligacaoAguaSituacao;
	}

	public void setLigacaoAguaSituacao(LigacaoAguaSituacao ligacaoAguaSituacao) {
		this.ligacaoAguaSituacao = ligacaoAguaSituacao;
	}

	public LigacaoEsgotoSituacao getLigacaoEsgotoSituacao() {
		return ligacaoEsgotoSituacao;
	}

	public void setLigacaoEsgotoSituacao(LigacaoEsgotoSituacao ligacaoEsgotoSituacao) {
		this.ligacaoEsgotoSituacao = ligacaoEsgotoSituacao;
	}
	
	public FaturamentoSituacaoTipo getFaturamentoSituacaoTipo() {
		return faturamentoSituacaoTipo;
	}

	public void setFaturamentoSituacaoTipo(FaturamentoSituacaoTipo faturamentoSituacaoTipo) {
		this.faturamentoSituacaoTipo = faturamentoSituacaoTipo;
	}

	public Imovel getImovelCondominio() {
		return imovelCondominio;
	}
	
	public void setImovelCondominio(Imovel imovel){
		this.imovelCondominio = imovel;
	}
	
	public Short getLote() {
		return lote;
	}

	public void setLote(Short lote) {
		this.lote = lote;
	}

	public Short getSubLote() {
		return subLote;
	}

	public void setSubLote(Short subLote) {
		this.subLote = subLote;
	}

	public Short getDiaVencimento() {
		return diaVencimento;
	}

	public void setDiaVencimento(Short diaVencimento) {
		this.diaVencimento = diaVencimento;
	}

	public Short getIndicadorEmissaoExtratoFaturamento() {
		return indicadorEmissaoExtratoFaturamento;
	}

	public void setIndicadorEmissaoExtratoFaturamento(Short indicadorEmissaoExtratoFaturamento) {
		this.indicadorEmissaoExtratoFaturamento = indicadorEmissaoExtratoFaturamento;
	}
	
	public Short getIndicadorVencimentoMesSeguinte() {
		return indicadorVencimentoMesSeguinte;
	}

	public void setIndicadorVencimentoMesSeguinte(Short indicadorVencimentoMesSeguinte) {
		this.indicadorVencimentoMesSeguinte = indicadorVencimentoMesSeguinte;
	}

	public Integer getImovelContaEnvio() {
		return imovelContaEnvio;
	}

	public void setImovelContaEnvio(Integer imovelContaEnvio) {
		this.imovelContaEnvio = imovelContaEnvio;
	}
	
	public Short getIndicadorDebitoConta() {
		return indicadorDebitoConta;
	}

	public void setIndicadorDebitoConta(Short indicadorDebitoConta) {
		this.indicadorDebitoConta = indicadorDebitoConta;
	}
	
	public Integer getPocoTipo() {
		return pocoTipo;
	}

	public void setPocoTipo(Integer pocoTipo) {
		this.pocoTipo = pocoTipo;
	}

	public HidrometroInstalacaoHistorico getHidrometroInstalacaoHistorico() {
		return hidrometroInstalacaoHistorico;
	}

	public void setHidrometroInstalacaoHistorico(HidrometroInstalacaoHistorico hidrometroInstalacaoHistorico) {
		this.hidrometroInstalacaoHistorico = hidrometroInstalacaoHistorico;
	}

	public Rota getRotaAlternativa() {
		return rotaAlternativa;
	}

	public void setRotaAlternativa(Rota rotaAlternativa) {
		this.rotaAlternativa = rotaAlternativa;
	}

	public Short getIndicadorExclusao() {
		return indicadorExclusao;
	}

	public void setIndicadorExclusao(Short indicadorExclusao) {
		this.indicadorExclusao = indicadorExclusao;
	}

	public Short getIndicadorImovelCondominio() {
		return indicadorImovelCondominio;
	}

	public void setIndicadorImovelCondominio(Short indicadorImovelCondominio) {
		this.indicadorImovelCondominio = indicadorImovelCondominio;
	}

	public ImovelPerfil getImovelPerfil() {
		return imovelPerfil;
	}

	public void setImovelPerfil(ImovelPerfil imovelPerfil) {
		this.imovelPerfil = imovelPerfil;
	}

	public List<ClienteImovel> getClienteImoveis() {
		return clienteImoveis;
	}

	public void setClienteImoveis(List<ClienteImovel> clienteImoveis) {
		this.clienteImoveis = clienteImoveis;
	}

	public LogradouroBairro getLogradouroBairro() {
		return logradouroBairro;
	}

	public void setLogradouroBairro(LogradouroBairro logradouroBairro) {
		this.logradouroBairro = logradouroBairro;
	}
	
	public EnderecoReferencia getEnderecoReferencia() {
		return enderecoReferencia;
	}

	public void setEnderecoReferencia(EnderecoReferencia enderecoReferencia) {
		this.enderecoReferencia = enderecoReferencia;
	}

	public Logradouro getPerimetroInicial() {
		return perimetroInicial;
	}

	public void setPerimetroInicial(Logradouro perimetroInicial) {
		this.perimetroInicial = perimetroInicial;
	}

	public Logradouro getPerimetroFinal() {
		return perimetroFinal;
	}

	public void setPerimetroFinal(Logradouro perimetroFinal) {
		this.perimetroFinal = perimetroFinal;
	}

	public String getComplementoEndereco() {
		return complementoEndereco;
	}

	public void setComplementoEndereco(String complementoEndereco) {
		this.complementoEndereco = complementoEndereco;
	}

	public LogradouroCep getLogradouroCep() {
		return logradouroCep;
	}

	public void setLogradouroCep(LogradouroCep logradouroCep) {
		this.logradouroCep = logradouroCep;
	}

	public ConsumoTarifa getConsumoTarifa() {
		return consumoTarifa;
	}

	public void setConsumoTarifa(ConsumoTarifa consumoTarifa) {
		this.consumoTarifa = consumoTarifa;
	}

	public BigDecimal getAreaConstruida() {
        return areaConstruida;
    }

    public void setAreaConstruida(BigDecimal areaConstruida) {
        this.areaConstruida = areaConstruida;
    }

    public AreaConstruidaFaixa getAreaConstruidaFaixa() {
        return areaConstruidaFaixa;
    }

    public void setAreaConstruidaFaixa(AreaConstruidaFaixa areaConstruidaFaixa) {
        this.areaConstruidaFaixa = areaConstruidaFaixa;
    }

    public String toString() {
		return "Imovel [id=" + id + ", numeroImovel=" + numeroImovel + "]";
	}
}
