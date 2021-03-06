package br.gov.servicos.operacao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.jboss.logging.Logger;

import br.gov.model.operacao.ConsumoEAT;
import br.gov.model.operacao.ConsumoETA;
import br.gov.model.operacao.EEABFonteCaptacao;
import br.gov.model.operacao.EEAT;
import br.gov.model.operacao.EEATFonteCaptacao;
import br.gov.model.operacao.ETA;
import br.gov.model.operacao.FonteCaptacaoProxy;
import br.gov.model.operacao.LancamentoPendente;
import br.gov.model.operacao.LocalidadeProxy;
import br.gov.model.operacao.MunicipioProxy;
import br.gov.model.operacao.PerfilBeanEnum;
import br.gov.model.operacao.Produto;
import br.gov.model.operacao.RegionalProxy;
import br.gov.model.operacao.SistemaAbastecimentoProxy;
import br.gov.model.operacao.UnidadeConsumidoraOperacional;
import br.gov.model.operacao.UnidadeNegocioProxy;
import br.gov.model.operacao.UsuarioProxy;

@Stateless
public class ProxyOperacionalRepositorio {

	private static Logger logger = Logger.getLogger(ProxyOperacionalRepositorio.class);

	@Resource(lookup = "java:/jboss/datasources/GsanDS")
	private DataSource dataSource;

	public List<List> selectRegistros(String sql) throws Exception {
		Statement stm = null;
		Connection con = null;
		con = dataSource.getConnection();
		stm = con.createStatement();

		List<List> listaGeral = new ArrayList();
		try {
			int coluna = 0, linha = 0, colunas = 0;
			ResultSet rs = stm.executeQuery(sql);
			ResultSetMetaData metadados = rs.getMetaData();
			colunas = metadados.getColumnCount();
			coluna = 0;

			while (rs.next()) {
				listaGeral.add(new ArrayList());
				while (coluna < colunas) {
					listaGeral.get(linha).add(rs.getString(coluna + 1));
					coluna++;
				}
				coluna = 0;
				linha++;
			}
			return listaGeral;
		} catch (SQLException e) {
			logger.error("Erro ao executar transacao", e);
			throw new Exception("Erro ao executar transacao", e);
		} finally {
			con.close();
		}
	}

	public List<RegionalProxy> getListaRegional() throws Exception {
		String query = "select greg_id, greg_nmregional from cadastro.gerencia_regional order by greg_nmregional";
		List<List> valores = selectRegistros(query);
		List<RegionalProxy> lista = new ArrayList<RegionalProxy>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			lista.add(new RegionalProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString()));
		}
		return lista;
	}

	public RegionalProxy getRegionalUnidadeNegocio(Integer codigoUnidadeNegocio) throws Exception {
		String query = "select A.greg_id, A.greg_nmregional from cadastro.gerencia_regional A inner join cadastro.unidade_negocio B ON A.greg_id = B.greg_id WHERE B.uneg_id = "
				+ codigoUnidadeNegocio;
		List<List> lista = selectRegistros(query);
		if (lista == null) {
			return null;
		}
		return new RegionalProxy(Integer.parseInt(lista.get(0).get(0).toString()), lista.get(0).get(1).toString());
	}

	public List<UnidadeNegocioProxy> getListaUnidadeNegocio(Integer codigoRegional) throws Exception {
		String query = "SELECT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio" + " FROM cadastro.gerencia_regional A"
				+ " INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id";

		if (codigoRegional != 0) {
			query = query + " WHERE A.greg_id = " + codigoRegional;
		}
		query = query + " ORDER BY B.uneg_nmunidadenegocio";

		List<List> valores = selectRegistros(query);
		List<UnidadeNegocioProxy> lista = new ArrayList<UnidadeNegocioProxy>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			lista.add(new UnidadeNegocioProxy(Integer.parseInt(colunas.get(2).toString()), colunas.get(3).toString()));
		}
		return lista;
	}

	public List<MunicipioProxy> getListaMunicipio(Integer codigoRegional, Integer codigoUnidadeNegocio) throws Exception {
		String query = "SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio, E.muni_id, E.muni_nmmunicipio"
				+ " FROM cadastro.gerencia_regional A" + " INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id"
				+ " INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID"
				+ " INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id" + " INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id"
				+ " WHERE B.uneg_id = " + codigoUnidadeNegocio;

		if (codigoRegional != 0) {
			query = query + " AND A.greg_id = " + codigoRegional;
		}
		query = query + " ORDER BY E.muni_nmmunicipio";

		List<List> valores = selectRegistros(query);
		List<MunicipioProxy> lista = new ArrayList<MunicipioProxy>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			lista.add(new MunicipioProxy(Integer.parseInt(colunas.get(4).toString()), colunas.get(5).toString()));
		}
		return lista;
	}

	public List<LocalidadeProxy> getListaLocalidade(Integer codigoRegional, Integer codigoUnidadeNegocio, Integer codigoMunicipio) throws Exception {
		String query = " SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio, "
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade" + " FROM cadastro.gerencia_regional A  "
				+ " INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ " INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ " INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id " + " INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id "
				+ " WHERE B.uneg_id =  " + codigoUnidadeNegocio + "   AND E.muni_id = " + codigoMunicipio;

		if (codigoRegional != 0) {
			query = query + " AND A.greg_id = " + codigoRegional;
		}
		query = query + " ORDER BY loca_nmlocalidade";

		List<List> valores = selectRegistros(query);
		List<LocalidadeProxy> lista = new ArrayList<LocalidadeProxy>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			lista.add(new LocalidadeProxy(Integer.parseInt(colunas.get(6).toString()), colunas.get(7).toString()));
		}
		return lista;
	}

	public List<SistemaAbastecimentoProxy> getListaSistemaAbastecimento(Integer codigoRegional, Integer codigoUnidadeNegocio, Integer codigoMunicipio,
			Integer localidade) throws Exception {
		String query = " SELECT A.* FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade, J.sabs_id, J.sabs_dssistemaabastecimento "
				+ " FROM cadastro.gerencia_regional A " + "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id " + "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id "
				+ "INNER JOIN cadastro.quadra F ON D.stcm_id = F.stcm_id " + "INNER JOIN cadastro.quadra_face G ON F.qdra_id = G.qdra_id "
				+ "INNER JOIN operacional.distrito_operacional H ON G.diop_id = H.diop_id "
				+ "INNER JOIN operacional.setor_abastecimento I ON H.stab_id = I.stab_id "
				+ "INNER JOIN operacional.sistema_abastecimento J ON I.sabs_id = J.sabs_id) AS A " + "WHERE greg_id = " + codigoRegional + "  AND uneg_id = "
				+ codigoUnidadeNegocio + "  AND muni_id = " + codigoMunicipio + "  AND loca_id = " + localidade + " ORDER BY sabs_dssistemaabastecimento";

		List<List> valores = selectRegistros(query);
		List<SistemaAbastecimentoProxy> lista = new ArrayList<SistemaAbastecimentoProxy>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			lista.add(new SistemaAbastecimentoProxy(Integer.parseInt(colunas.get(8).toString()), colunas.get(9).toString()));
		}
		return lista;
	}

	public List<FonteCaptacaoProxy> getListaFonteCaptacao() throws Exception {
		String query = "select ftcp_id, ftcp_dsfontecaptacao from operacional.fonte_captacao order by ftcp_dsfontecaptacao";
		List<List> valores = selectRegistros(query);
		List<FonteCaptacaoProxy> lista = new ArrayList<FonteCaptacaoProxy>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			lista.add(new FonteCaptacaoProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString()));
		}
		return lista;
	}

	public List<UnidadeConsumidoraOperacional> getListaUnidadeOperacional(Integer tipoUnidade) throws Exception {

		String query = "";
		if (tipoUnidade == 1) {// EAB
			query = "select eeab_id, eeab_nome from operacao.eeab order by eeab_nome";
		} else if (tipoUnidade == 2) {// ETA
			query = "select eta_id, eta_nome from operacao.eta order by eta_nome";
		} else if (tipoUnidade == 3) {// EAT
			query = "select eeat_id, eeat_nome from operacao.eeat order by eeat_nome";
		} else if (tipoUnidade == 4) {// RSO
			query = "select rso_id, rso_nome from operacao.rso order by rso_nome";
		} else if (tipoUnidade == 5) {// RESIDÊNCIA
			query = "select resd_id, resd_nome from operacao.residencia order by resd_nome";
		} else if (tipoUnidade == 6) {// ESCRITÓRIO
			query = "select escr_id, escr_nome from operacao.escritorio order by escr_nome";
		}
		List<List> valores = selectRegistros(query);
		List<UnidadeConsumidoraOperacional> lista = new ArrayList<UnidadeConsumidoraOperacional>();
		UnidadeConsumidoraOperacional undOpe;

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			undOpe = new UnidadeConsumidoraOperacional();
			undOpe.setCodigo(Integer.parseInt(colunas.get(0).toString()));
			undOpe.setCodigoUnidadeOperacional(Integer.parseInt(colunas.get(0).toString()));
			undOpe.setDescricao(colunas.get(1).toString());
			lista.add(undOpe);
		}
		return lista;
	}

	public String getUnidadeOperacional(Integer tipoUnidade, Integer codigoUnidade) throws Exception {

		String query = "";
		if (tipoUnidade == 1) {// EAB
			query = "select eeab_id, eeab_nome from operacao.eeab where eeab_id = " + codigoUnidade;
		} else if (tipoUnidade == 2) {// ETA
			query = "select eta_id, eta_nome from operacao.eta where eta_id = " + codigoUnidade;
		} else if (tipoUnidade == 3) {// EAT
			query = "select eeat_id, eeat_nome from operacao.eeat where eeat_id = " + codigoUnidade;
		} else if (tipoUnidade == 4) {// RSO
			query = "select rso_id, rso_nome from operacao.rso where rso_id = " + codigoUnidade;
		} else if (tipoUnidade == 5) {// RESIDÊNCIA
			query = "select resd_id,  resd_nome from operacao.residencia where resd_id = " + codigoUnidade;
		} else if (tipoUnidade == 6) {// ESCRITÓRIO
			query = "select escr_id, escr_nome from operacao.escritorio where escr_id = " + codigoUnidade;
		}
		List<List> lista = selectRegistros(query);
		if (lista == null) {
			return null;
		}
		return (lista.get(0).get(1).toString());
	}

	public List<EEABFonteCaptacao> getListaFonteCaptacaoEEAB(Integer tipoFonte) throws Exception {

		String query;
		if (tipoFonte == 1) {// Fonte Interna EEAB
			query = "select eeab_id, eeab_nome from operacao.eeab order by eeab_nome";
		} else {// Fonte Externa
			query = "select ftcp_id, ftcp_dsfontecaptacao from operacional.fonte_captacao order by ftcp_dsfontecaptacao";
		}
		List<List> valores = selectRegistros(query);
		List<EEABFonteCaptacao> lista = new ArrayList<EEABFonteCaptacao>();
		EEABFonteCaptacao fonte;

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			fonte = new EEABFonteCaptacao();
			fonte.setCodigo(Integer.parseInt(colunas.get(0).toString()));
			fonte.setCodigoFonte(Integer.parseInt(colunas.get(0).toString()));
			fonte.setNomeFonte(colunas.get(1).toString());
			lista.add(fonte);
		}
		return lista;
	}

	public String getFonteCaptacaoEEAB(Integer tipoFonte, Integer codigoFonte) throws Exception {

		String query;
		if (tipoFonte == 1) {// Fonte Interna EEAB
			query = "select eeab_id, eeab_nome from operacao.eeab where eeab_id = " + codigoFonte;
		} else {// Fonte Externa
			query = "select ftcp_id, ftcp_dsfontecaptacao from operacional.fonte_captacao where ftcp_id = " + codigoFonte;
		}
		List<List> lista = selectRegistros(query);
		if (lista == null) {
			return null;
		}
		return (lista.get(0).get(1).toString());
	}

	public List<EEATFonteCaptacao> getListaFonteCaptacaoEEAT(Integer tipoFonte) throws Exception {

		String query;
		if (tipoFonte == 1) {// Fonte Interna EEAB
			query = "select eeat_id, eeat_nome from operacao.eeat order by eeat_nome";
		} else {// Fonte Externa
			query = "select eta_id, eta_nome from operacao.eta order by eta_nome";
		}
		List<List> valores = selectRegistros(query);
		List<EEATFonteCaptacao> lista = new ArrayList<EEATFonteCaptacao>();
		EEATFonteCaptacao fonte;

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			fonte = new EEATFonteCaptacao();
			fonte.setCodigo(Integer.parseInt(colunas.get(0).toString()));
			fonte.setCodigoFonte(Integer.parseInt(colunas.get(0).toString()));
			fonte.setNomeFonte(colunas.get(1).toString());
			lista.add(fonte);
		}
		return lista;
	}

	public String getFonteCaptacaoEEAT(Integer tipoFonte, Integer codigoFonte) throws Exception {

		String query;
		if (tipoFonte == 1) {// EEAT
			query = "select eeat_id, eeat_nome from operacao.eeat where eeat_id = " + codigoFonte;
		} else {// ETA
			query = "select eta_id, eta_nome from operacao.eta where eta_id = " + codigoFonte;
		}
		List<List> lista = selectRegistros(query);
		if (lista == null) {
			return null;
		}
		return (lista.get(0).get(1).toString());
	}

	public UnidadeNegocioProxy getUnidadeNegocio(Integer codigo) throws Exception {
		String query = "SELECT B.uneg_id, B.uneg_nmunidadenegocio FROM cadastro.unidade_negocio B WHERE B.uneg_id = " + codigo;
		List<List> lista = this.selectRegistros(query);
		if (lista == null) {
			return null;
		}
		return new UnidadeNegocioProxy(Integer.parseInt(lista.get(0).get(0).toString()), lista.get(0).get(1).toString());
	}

	public SistemaAbastecimentoProxy getSistemaAbastecimento(Integer codigo) throws Exception {
		String query = "Select J.sabs_id, J.sabs_dssistemaabastecimento FROM operacional.sistema_abastecimento J WHERE J.sabs_id = " + codigo;
		List<List> lista = this.selectRegistros(query);
		if (lista == null) {
			return null;
		}
		return new SistemaAbastecimentoProxy(Integer.parseInt(lista.get(0).get(0).toString()), lista.get(0).get(1).toString());
	}

	public LocalidadeProxy getLocalidade(Integer codigo) throws Exception {
		String query = "Select L.loca_id, L.loca_nmlocalidade FROM cadastro.localidade L WHERE L.loca_id = " + codigo;
		List<List> lista = this.selectRegistros(query);
		if (lista == null) {
			return null;
		}
		return new LocalidadeProxy(Integer.parseInt(lista.get(0).get(0).toString()), lista.get(0).get(1).toString());
	}

	public MunicipioProxy getMunicipio(Integer codigo) throws Exception {
		String query = "Select K.muni_id, K.muni_nmmunicipio FROM cadastro.municipio K WHERE K.muni_id = " + codigo;
		List<List> lista = this.selectRegistros(query);
		if (lista == null) {
			return null;
		}
		return new MunicipioProxy(Integer.parseInt(lista.get(0).get(0).toString()), lista.get(0).get(1).toString());
	}

	public RegionalProxy getRegional(Integer codigo) throws Exception {
		String query = "Select P.greg_id, P.greg_nmregional FROM cadastro.gerencia_regional P WHERE P.greg_id = " + codigo;
		List<List> lista = this.selectRegistros(query);
		if (lista == null) {
			return null;
		}
		return new RegionalProxy(Integer.parseInt(lista.get(0).get(0).toString()), lista.get(0).get(1).toString());
	}

	public List<LancamentoPendente> getUnidadeOperacionalUsuario(UsuarioProxy usuario) throws Exception {

		String localidade = "";
		String query = "SELECT A.* FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade" + " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id " + "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) AS A ";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " WHERE A.greg_id = " + usuario.getRegionalProxy().getCodigo();
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " WHERE A.loca_id IN (" + localidade + ")";
		}

		List<List> valores = selectRegistros(query);
		List<LancamentoPendente> lista = new ArrayList<LancamentoPendente>();

		if (valores == null) {
			return lista;
		}

		Integer intI = 1;
		for (List colunas : valores) {
			LancamentoPendente consumo = new LancamentoPendente();
			consumo.setCodigo(intI++);
			consumo.setRegionalProxy(new RegionalProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString()));
			consumo.setUnidadeNegocioProxy(new UnidadeNegocioProxy(Integer.parseInt(colunas.get(2).toString()), colunas.get(3).toString()));
			consumo.setMunicipioProxy(new MunicipioProxy(Integer.parseInt(colunas.get(4).toString()), colunas.get(5).toString()));
			consumo.setLocalidadeProxy(new LocalidadeProxy(Integer.parseInt(colunas.get(6).toString()), colunas.get(7).toString()));
			lista.add(consumo);
		}

		return lista;
	}

	private List<LancamentoPendente> getListaEABUsuario(UsuarioProxy usuario) throws Exception {

		String localidade = "";
		String query = "SELECT A.*, D.eeab_id, D.eeab_nome, C.ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade"
				+ " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
				+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) AS A "
				+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
				+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
				+ "INNER JOIN operacao.eeab D ON C.ucop_idoperacional = D.eeab_id " + "WHERE C.ucop_tipooperacional = 1";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " AND A.greg_id = " + usuario.getRegionalProxy().getCodigo();
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " AND A.loca_id IN (" + localidade + ")";
		}

		List<List> valores = selectRegistros(query);
		List<LancamentoPendente> lista = new ArrayList<LancamentoPendente>();

		if (valores == null) {
			return lista;
		}

		Integer intI = 1;
		for (List colunas : valores) {
			LancamentoPendente lancamento = new LancamentoPendente();
			lancamento.setCodigo(intI++);
			lancamento.setRegionalProxy(new RegionalProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString()));
			lancamento.setUnidadeNegocioProxy(new UnidadeNegocioProxy(Integer.parseInt(colunas.get(2).toString()), colunas.get(3).toString()));
			lancamento.setMunicipioProxy(new MunicipioProxy(Integer.parseInt(colunas.get(4).toString()), colunas.get(5).toString()));
			lancamento.setLocalidadeProxy(new LocalidadeProxy(Integer.parseInt(colunas.get(6).toString()), colunas.get(7).toString()));
			lancamento.setCodigoUnidadeOperacional(Integer.parseInt(colunas.get(8).toString()));
			lancamento.setUnidadeOperacional(colunas.get(9).toString());
			lancamento.setTipoUnidadeOperacional(Integer.parseInt(colunas.get(10).toString()));
			lista.add(lancamento);
		}
		return lista;
	}

	private List<LancamentoPendente> getListaETAUsuario(UsuarioProxy usuario) throws Exception {

		String localidade = "";
		String query = "SELECT A.*, D.eta_id, D.eta_nome, C.ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade"
				+ " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
				+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) AS A "
				+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
				+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
				+ "INNER JOIN operacao.eta D ON C.ucop_idoperacional = D.eta_id " + "WHERE C.ucop_tipooperacional = 2";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " AND A.greg_id = " + usuario.getRegionalProxy().getCodigo();
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " AND A.loca_id IN (" + localidade + ")";
		}

		List<List> valores = selectRegistros(query);
		List<LancamentoPendente> lista = new ArrayList<LancamentoPendente>();

		if (valores == null) {
			return lista;
		}

		Integer intI = 1;
		for (List colunas : valores) {
			LancamentoPendente lancamento = new LancamentoPendente();
			lancamento.setCodigo(intI++);
			lancamento.setRegionalProxy(new RegionalProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString()));
			lancamento.setUnidadeNegocioProxy(new UnidadeNegocioProxy(Integer.parseInt(colunas.get(2).toString()), colunas.get(3).toString()));
			lancamento.setMunicipioProxy(new MunicipioProxy(Integer.parseInt(colunas.get(4).toString()), colunas.get(5).toString()));
			lancamento.setLocalidadeProxy(new LocalidadeProxy(Integer.parseInt(colunas.get(6).toString()), colunas.get(7).toString()));
			lancamento.setCodigoUnidadeOperacional(Integer.parseInt(colunas.get(8).toString()));
			lancamento.setUnidadeOperacional(colunas.get(9).toString());
			lancamento.setTipoUnidadeOperacional(Integer.parseInt(colunas.get(10).toString()));
			lista.add(lancamento);
		}
		return lista;
	}

	private List<LancamentoPendente> getListaEATUsuario(UsuarioProxy usuario) throws Exception {

		String localidade = "";
		String query = "SELECT A.*, D.eeat_id, D.eeat_nome, C.ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade"
				+ " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
				+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) AS A "
				+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
				+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
				+ "INNER JOIN operacao.eeat D ON C.ucop_idoperacional = D.eeat_id " + "WHERE C.ucop_tipooperacional = 3";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " AND A.greg_id = " + usuario.getRegionalProxy().getCodigo();
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " AND A.loca_id IN (" + localidade + ")";
		}

		List<List> valores = selectRegistros(query);
		List<LancamentoPendente> lista = new ArrayList<LancamentoPendente>();

		if (valores == null) {
			return lista;
		}

		Integer intI = 1;
		for (List colunas : valores) {
			LancamentoPendente lancamento = new LancamentoPendente();
			lancamento.setCodigo(intI++);
			lancamento.setRegionalProxy(new RegionalProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString()));
			lancamento.setUnidadeNegocioProxy(new UnidadeNegocioProxy(Integer.parseInt(colunas.get(2).toString()), colunas.get(3).toString()));
			lancamento.setMunicipioProxy(new MunicipioProxy(Integer.parseInt(colunas.get(4).toString()), colunas.get(5).toString()));
			lancamento.setLocalidadeProxy(new LocalidadeProxy(Integer.parseInt(colunas.get(6).toString()), colunas.get(7).toString()));
			lancamento.setCodigoUnidadeOperacional(Integer.parseInt(colunas.get(8).toString()));
			lancamento.setUnidadeOperacional(colunas.get(9).toString());
			lancamento.setTipoUnidadeOperacional(Integer.parseInt(colunas.get(10).toString()));
			lista.add(lancamento);
		}
		return lista;
	}

	private List<LancamentoPendente> getListaRSOUsuario(UsuarioProxy usuario) throws Exception {

		String localidade = "";
		String query = "SELECT A.*, D.rso_id, D.rso_nome, C.ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade"
				+ " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
				+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) AS A "
				+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
				+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
				+ "INNER JOIN operacao.rso D ON C.ucop_idoperacional = D.rso_id " + "WHERE C.ucop_tipooperacional = 4";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " AND A.greg_id = " + usuario.getRegionalProxy().getCodigo();
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " AND A.loca_id IN (" + localidade + ")";
		}

		List<List> valores = selectRegistros(query);
		List<LancamentoPendente> lista = new ArrayList<LancamentoPendente>();

		if (valores == null) {
			return lista;
		}

		Integer intI = 1;
		for (List colunas : valores) {
			LancamentoPendente lancamento = new LancamentoPendente();
			lancamento.setCodigo(intI++);
			lancamento.setRegionalProxy(new RegionalProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString()));
			lancamento.setUnidadeNegocioProxy(new UnidadeNegocioProxy(Integer.parseInt(colunas.get(2).toString()), colunas.get(3).toString()));
			lancamento.setMunicipioProxy(new MunicipioProxy(Integer.parseInt(colunas.get(4).toString()), colunas.get(5).toString()));
			lancamento.setLocalidadeProxy(new LocalidadeProxy(Integer.parseInt(colunas.get(6).toString()), colunas.get(7).toString()));
			lancamento.setCodigoUnidadeOperacional(Integer.parseInt(colunas.get(8).toString()));
			lancamento.setUnidadeOperacional(colunas.get(9).toString());
			lancamento.setTipoUnidadeOperacional(Integer.parseInt(colunas.get(10).toString()));
			lista.add(lancamento);
		}
		return lista;
	}

	public List<ConsumoETA> getListaConsumoETAUsuario(UsuarioProxy usuario) throws Exception {

		String localidade = "";
		String query = "SELECT A.* FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade, G.eta_id, G.eta_nome "
				+ " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
				+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id "
				+ "INNER JOIN operacao.registroconsumoeta F ON A.greg_id = F.greg_id AND B.uneg_id = F.uneg_id AND C.loca_id = F.loca_id AND E.muni_id = F.muni_id "
				+ "INNER JOIN operacao.eta G ON F.eta_id = G.eta_id) AS A ";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " WHERE A.greg_id = " + usuario.getRegionalProxy().getCodigo();
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " WHERE A.loca_id IN (" + localidade + ")";
		}

		List<List> valores = selectRegistros(query);
		List<ConsumoETA> lista = new ArrayList<ConsumoETA>();

		if (valores == null) {
			return lista;
		}

		Integer intI = 1;
		for (List colunas : valores) {
			ConsumoETA consumo = new ConsumoETA();
			consumo.setCodigo(intI++);
			consumo.setRegionalProxy(new RegionalProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString()));
			consumo.setUnidadeNegocioProxy(new UnidadeNegocioProxy(Integer.parseInt(colunas.get(2).toString()), colunas.get(3).toString()));
			consumo.setMunicipioProxy(new MunicipioProxy(Integer.parseInt(colunas.get(4).toString()), colunas.get(5).toString()));
			consumo.setLocalidadeProxy(new LocalidadeProxy(Integer.parseInt(colunas.get(6).toString()), colunas.get(7).toString()));
			consumo.setEta(new ETA(Integer.parseInt(colunas.get(8).toString()), colunas.get(9).toString()));
			lista.add(consumo);
		}

		return lista;
	}

	public List<ConsumoEAT> getListaConsumoEATUsuario(UsuarioProxy usuario) throws Exception {

		String localidade = "";
		String query = "SELECT A.* FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade, F.eat_id, G.eeat_nome "
				+ " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
				+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id "
				+ "INNER JOIN operacao.registroconsumoeat F ON A.greg_id = F.greg_id AND B.uneg_id = F.uneg_id AND C.loca_id = F.loca_id AND E.muni_id = F.muni_id "
				+ "INNER JOIN operacao.eeat G ON F.eat_id = G.eeat_id) AS A ";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " WHERE A.greg_id = " + usuario.getRegionalProxy().getCodigo();
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " WHERE A.loca_id IN (" + localidade + ")";
		}

		List<List> valores = selectRegistros(query);
		List<ConsumoEAT> lista = new ArrayList<ConsumoEAT>();

		if (valores == null) {
			return lista;
		}

		Integer intI = 1;
		for (List colunas : valores) {
			ConsumoEAT consumo = new ConsumoEAT();
			consumo.setCodigo(intI++);
			consumo.setRegionalProxy(new RegionalProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString()));
			consumo.setUnidadeNegocioProxy(new UnidadeNegocioProxy(Integer.parseInt(colunas.get(2).toString()), colunas.get(3).toString()));
			consumo.setMunicipioProxy(new MunicipioProxy(Integer.parseInt(colunas.get(4).toString()), colunas.get(5).toString()));
			consumo.setLocalidadeProxy(new LocalidadeProxy(Integer.parseInt(colunas.get(6).toString()), colunas.get(7).toString()));
			consumo.setEat(new EEAT(Integer.parseInt(colunas.get(8).toString()), colunas.get(9).toString()));
			lista.add(consumo);
		}

		return lista;
	}

	public String[] getConsumoSistemaAbastecimento(Integer codigoRegional, Integer codigoUnidadeNegocio, Integer codigoMunicipio, Integer codigoLocalidade,
			Integer codigoSistemaAbastecimento, Date dataConsumo, Integer codigoProduto) throws Exception {

		SimpleDateFormat formataData = new SimpleDateFormat("yyyyMMdd");
		String dataAux = formataData.format(dataConsumo);

		String query = "SELECT A.conp_quantidade, A.cons_id" + " FROM operacao.consumo_produto A" + " INNER JOIN operacao.consumo B ON A.cons_id = B.cons_id"
				+ " WHERE A.prod_id = " + codigoProduto + " AND B.greg_id = " + codigoRegional + " AND B.uneg_id = " + codigoUnidadeNegocio
				+ " AND B.muni_id = " + codigoMunicipio + " AND B.loca_id = " + codigoLocalidade + " AND B.sabs_id = " + codigoSistemaAbastecimento
				+ " AND B.cons_data = '" + dataAux + "'";

		List<List> valores = selectRegistros(query);
		String consumo[] = new String[2];

		if (valores == null) {
			return consumo;
		}

		for (List colunas : valores) {
			consumo[0] = colunas.get(0).toString();
			consumo[1] = colunas.get(1).toString();
		}
		return consumo;
	}

	public String[] getConsumoETA(Integer codigoRegional, Integer codigoUnidadeNegocio, Integer codigoMunicipio, Integer codigoLocalidade, Integer codigoETA,
			Date dataConsumo, Integer codigoProduto) throws Exception {

		SimpleDateFormat formataData = new SimpleDateFormat("yyyyMMdd");
		String dataAux = formataData.format(dataConsumo);

		String query = "SELECT A.conp_quantidade, A.cons_id" + " FROM operacao.consumoeta_produto A"
				+ " INNER JOIN operacao.consumoeta B ON A.cons_id = B.cons_id" + " WHERE A.prod_id = " + codigoProduto + " AND B.greg_id = " + codigoRegional
				+ " AND B.uneg_id = " + codigoUnidadeNegocio + " AND B.muni_id = " + codigoMunicipio + " AND B.loca_id = " + codigoLocalidade
				+ " AND B.eta_id = " + codigoETA + " AND B.cons_data = '" + dataAux + "'";

		List<List> valores = selectRegistros(query);
		String consumo[] = new String[2];

		if (valores == null) {
			return consumo;
		}

		for (List colunas : valores) {
			consumo[0] = colunas.get(0).toString();
			consumo[1] = colunas.get(1).toString();
		}
		return consumo;
	}

	public String[] getConsumoEAT(Integer codigoRegional, Integer codigoUnidadeNegocio, Integer codigoMunicipio, Integer codigoLocalidade, Integer codigoEAT,
			Date dataConsumo, Integer codigoProduto) throws Exception {

		SimpleDateFormat formataData = new SimpleDateFormat("yyyyMMdd");
		String dataAux = formataData.format(dataConsumo);

		String query = "SELECT A.conp_quantidade, A.cons_id" + " FROM operacao.consumoeat_produto A"
				+ " INNER JOIN operacao.consumoeat B ON A.cons_id = B.cons_id" + " WHERE A.prod_id = " + codigoProduto + " AND B.greg_id = " + codigoRegional
				+ " AND B.uneg_id = " + codigoUnidadeNegocio + " AND B.muni_id = " + codigoMunicipio + " AND B.loca_id = " + codigoLocalidade
				+ " AND B.eat_id = " + codigoEAT + " AND B.cons_data = '" + dataAux + "'";

		List<List> valores = selectRegistros(query);
		String consumo[] = new String[2];

		if (valores == null) {
			return consumo;
		}

		for (List colunas : valores) {
			consumo[0] = colunas.get(0).toString();
			consumo[1] = colunas.get(1).toString();
		}
		return consumo;
	}

	public List<Produto> getProdutosSistemaAbastecimento(Integer codigoRegional, Integer codigoUnidadeNegocio, Integer codigoMunicipio,
			Integer codigoLocalidade, Integer codigoSistemaAbastecimento, Date dataInicial, Date dataFinal) throws Exception {

		SimpleDateFormat formataData = new SimpleDateFormat("yyyyMMdd");
		GregorianCalendar gc = new GregorianCalendar();
		// Formata Data Inicial
		gc.setTime(dataInicial);
		// gc.set(GregorianCalendar.YEAR, 1970);
		String dataIni = formataData.format(gc.getTime());

		// Formata Data Final
		gc.setTime(dataFinal);
		// gc.set(GregorianCalendar.YEAR, 1970);
		// String dataFim = formataData.format(gc.getTime());

		String query = "SELECT DISTINCT A.prod_id, A.prod_nmproduto" + " FROM operacao.produto A"
				+ " INNER JOIN operacao.registroconsumo_produto B ON A.prod_id = B.prod_id" + " INNER JOIN operacao.registroconsumo C ON B.regc_id = C.regc_id"
				+ " INNER JOIN operacao.registroconsumosistemaabastecimento_registroconsumo D ON C.regc_id = D.regc_id"
				+ " INNER JOIN operacao.registroconsumosistemaabastecimento E ON D.rgcs_id = E.rgcs_id" + " WHERE E.greg_id = " + codigoRegional
				+ " AND E.uneg_id = " + codigoUnidadeNegocio + " AND E.muni_id = " + codigoMunicipio + " AND E.loca_id = " + codigoLocalidade
				+ " AND E.sabs_id = " + codigoSistemaAbastecimento + " AND C.regc_dataini <= '" + dataIni + "'" + " AND C.regc_datafim >= '" + dataIni + "'";

		List<List> valores = selectRegistros(query);
		List<Produto> lista = new ArrayList<Produto>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			Produto produto = new Produto();
			produto.setCodigo(Integer.parseInt(colunas.get(0).toString()));
			produto.setDescricao(colunas.get(1).toString());
			lista.add(produto);
		}
		return lista;
	}

	public List<Produto> getListaProdutoETA(Integer codigoRegional, Integer codigoUnidadeNegocio, Integer codigoMunicipio, Integer codigoLocalidade,
			Integer codigoETA, Date dataInicial, Date dataFinal) throws Exception {

		SimpleDateFormat formataData = new SimpleDateFormat("yyyyMMdd");
		GregorianCalendar gc = new GregorianCalendar();
		// Formata Data Inicial
		gc.setTime(dataInicial);
		String dataIni = formataData.format(gc.getTime());
		// Formata Data Final
		gc.setTime(dataFinal);

		String query = "SELECT DISTINCT A.prod_id, A.prod_nmproduto" + " FROM operacao.produto A"
				+ " INNER JOIN operacao.registroconsumo_produto B ON A.prod_id = B.prod_id" + " INNER JOIN operacao.registroconsumo C ON B.regc_id = C.regc_id"
				+ " INNER JOIN operacao.registroconsumoeta_registroconsumo D ON C.regc_id = D.regc_id"
				+ " INNER JOIN operacao.registroconsumoeta E ON D.rgcs_id = E.rgcs_id" + " WHERE E.greg_id = " + codigoRegional + " AND E.uneg_id = "
				+ codigoUnidadeNegocio + " AND E.muni_id = " + codigoMunicipio + " AND E.loca_id = " + codigoLocalidade + " AND E.eta_id = " + codigoETA
				+ " AND C.regc_dataini <= '" + dataIni + "'" + " AND C.regc_datafim >= '" + dataIni + "'";

		List<List> valores = selectRegistros(query);
		List<Produto> lista = new ArrayList<Produto>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			Produto produto = new Produto();
			produto.setCodigo(Integer.parseInt(colunas.get(0).toString()));
			produto.setDescricao(colunas.get(1).toString());
			lista.add(produto);
		}
		return lista;
	}

	public List<Produto> getListaProdutoEAT(Integer codigoRegional, Integer codigoUnidadeNegocio, Integer codigoMunicipio, Integer codigoLocalidade,
			Integer codigoEAT, Date dataInicial, Date dataFinal) throws Exception {

		SimpleDateFormat formataData = new SimpleDateFormat("yyyyMMdd");
		GregorianCalendar gc = new GregorianCalendar();
		// Formata Data Inicial
		gc.setTime(dataInicial);
		String dataIni = formataData.format(gc.getTime());
		// Formata Data Final
		gc.setTime(dataFinal);

		String query = "SELECT DISTINCT A.prod_id, A.prod_nmproduto" + " FROM operacao.produto A"
				+ " INNER JOIN operacao.registroconsumo_produto B ON A.prod_id = B.prod_id" + " INNER JOIN operacao.registroconsumo C ON B.regc_id = C.regc_id"
				+ " INNER JOIN operacao.registroconsumoeat_registroconsumo D ON C.regc_id = D.regc_id"
				+ " INNER JOIN operacao.registroconsumoeat E ON D.rgcs_id = E.rgcs_id" + " WHERE E.greg_id = " + codigoRegional + " AND E.uneg_id = "
				+ codigoUnidadeNegocio + " AND E.muni_id = " + codigoMunicipio + " AND E.loca_id = " + codigoLocalidade + " AND E.eat_id = " + codigoEAT
				+ " AND C.regc_dataini <= '" + dataIni + "'" + " AND C.regc_datafim >= '" + dataIni + "'";

		List<List> valores = selectRegistros(query);
		List<Produto> lista = new ArrayList<Produto>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			Produto produto = new Produto();
			produto.setCodigo(Integer.parseInt(colunas.get(0).toString()));
			produto.setDescricao(colunas.get(1).toString());
			lista.add(produto);
		}
		return lista;
	}

	public UsuarioProxy getPerfilUsuario(UsuarioProxy usuarioProxy) throws Exception {

		String query = "SELECT A.unid_id, B.unid_idsuperior, B.unid_dsunidade, B.loca_id, B.greg_id, C.loca_nmlocalidade, D.greg_nmregional, A.usur_nmlogin"
				+ "  FROM seguranca.usuario A" + " INNER JOIN cadastro.unidade_organizacional B ON A.unid_id = B.unid_id"
				+ " LEFT JOIN cadastro.localidade C ON B.loca_id = C.loca_id" + " LEFT JOIN cadastro.gerencia_regional D ON B.greg_id = D.greg_id"
				+ " WHERE A.usur_id = " + usuarioProxy.getCodigo();

		List<List> valores = selectRegistros(query);
		List<LocalidadeProxy> localidadeProxy;

		usuarioProxy.setAdministrador(false);
		usuarioProxy.setLogado(false);
		for (List colunas : valores) {
			usuarioProxy.setLogado(true);
			usuarioProxy.setNome(colunas.get(7).toString());
			usuarioProxy.setUnidadeOrganizacional(colunas.get(2).toString());
			// Se Unidade Superior é vazio ou 1, então é ADMIN
			if (colunas.get(1) == null || colunas.get(1).toString().equals("1")) {
				usuarioProxy.setPerfil(PerfilBeanEnum.ADMIN);
				usuarioProxy.setAdministrador(true);
			}
			// Se Localidade preenchida, então acesso somente a localidade
			else if (colunas.get(3) != null) {
				usuarioProxy.setPerfil(PerfilBeanEnum.SUPERVISOR);
				localidadeProxy = new ArrayList<LocalidadeProxy>();
				localidadeProxy.add(new LocalidadeProxy(Integer.parseInt(colunas.get(3).toString()), colunas.get(5).toString()));
				usuarioProxy.setLocalidadeProxy(localidadeProxy);
			}
			// Se Gerência Regional preenchida, então acesso as localidades da
			// gerência Regional
			else if (colunas.get(4) != null) {
				usuarioProxy.setPerfil(PerfilBeanEnum.GERENTE);
				usuarioProxy.setRegionalProxy(new RegionalProxy(Integer.parseInt(colunas.get(4).toString()), colunas.get(6).toString()));
			}
			// Não possui Gerência Regional Preenchida, então acesso as
			// localidades relacionadas a unidade organizacional
			else {
				usuarioProxy.setPerfil(PerfilBeanEnum.COORDENADOR);

				query = "SELECT A.unid_id, A.unid_idsuperior, A.unid_dsunidade, A.loca_id, B.loca_nmlocalidade" + "  FROM cadastro.unidade_organizacional A"
						+ "  LEFT JOIN cadastro.localidade B ON A.loca_id = B.loca_id" + " WHERE A.loca_id IS NOT NULL" + "   AND A.unid_idsuperior = "
						+ colunas.get(0).toString();

				List<List> unidade = selectRegistros(query);
				localidadeProxy = new ArrayList<LocalidadeProxy>();
				for (List local : unidade) {
					localidadeProxy.add(new LocalidadeProxy(Integer.parseInt(local.get(3).toString()), local.get(4).toString()));
				}
				usuarioProxy.setLocalidadeProxy(localidadeProxy);
			}
		}

		return (usuarioProxy);
	}

	public UsuarioProxy getParametrosSistema(UsuarioProxy usuarioProxy) throws Exception {

		// Referência de Faturamento
		String query = "SELECT parm_amreferenciafaturamento FROM cadastro.sistema_parametros";

		List<List> valores = selectRegistros(query);

		for (List colunas : valores) {
			usuarioProxy.setReferencia(Integer.parseInt(colunas.get(0).toString()));
		}

		// Dias de Lançamento Pendentes
		query = "SELECT parm_valor FROM operacao.parametro WHERE parm_nmparametro = 'DIAS_PENDENCIA_LANCAMENTO'";

		valores = selectRegistros(query);

		for (List colunas : valores) {
			usuarioProxy.setDiaPendencia(Integer.parseInt(colunas.get(0).toString()));
		}

		return usuarioProxy;
	}

	public String getParametroSistema(Integer codigo) throws Exception {
		// Dias de Lançamento Pendentes
		String query = "SELECT parm_valor FROM operacao.parametro WHERE parm_id = " + codigo;

		List<List> valores = selectRegistros(query);

		for (List colunas : valores) {
			return colunas.get(0).toString();
		}
		return null;
	}

	public List<LancamentoPendente> getConsumoPendenteUsuario(UsuarioProxy usuarioProxy, Integer tipoConsulta) throws Exception {

		List<LancamentoPendente> pendencias = new ArrayList<LancamentoPendente>();
		String filtroLocalidade = "";

		// Recuperando Dias Pendencias
		Integer diasPendencia = usuarioProxy.getDiaPendencia();
		if (diasPendencia != 0) {// Controle de Pendências Desabilitado
			if (usuarioProxy.getPerfil() == PerfilBeanEnum.ADMIN || usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE
					|| usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR || usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR) {

				SimpleDateFormat formataData = new SimpleDateFormat("yyyyMMdd");
				GregorianCalendar gc = new GregorianCalendar();
				// Recuperando Mês de Referência Atual
				String referencia = usuarioProxy.getReferencia().toString();
				// Recuperando Primeiro Dia do Mês
				gc.set(Integer.parseInt(referencia.substring(0, 4)), Integer.parseInt(referencia.substring(4, 6)) - 1, 1, 0, 0, 0);

				Date dtIni = gc.getTime();
				String dataIni = formataData.format(dtIni);

				// Recuperando Último Dia do Mês
				gc.add(Calendar.MONTH, 1);
				gc.add(Calendar.DATE, -1);
				Date dtFim = gc.getTime();
				String dataFim = formataData.format(dtFim);

				String query = "SELECT cons_data, A.*, C.eta_id, C.eta_nome, 2 AS unop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
						+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade"
						+ " FROM cadastro.gerencia_regional A "
						+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
						+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
						+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
						+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) A "
						+ "INNER JOIN operacao.consumoeta B ON A.greg_id = B.greg_id AND A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
						+ "INNER JOIN operacao.eta C ON B.eta_id = C.eta_id" + " WHERE B.cons_data BETWEEN '" + dataIni + "' AND '" + dataFim + "'";

				// Se Perfil de Gerente, acesso a gerência Regional
				if (usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE) {
					query = query + " AND A.greg_id = " + usuarioProxy.getRegionalProxy().getCodigo();
				} else if (usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR) {
					for (LocalidadeProxy colunas : usuarioProxy.getLocalidadeProxy()) {
						filtroLocalidade = filtroLocalidade + colunas.getCodigo() + ",";
					}
					filtroLocalidade = filtroLocalidade.substring(0, filtroLocalidade.length() - 1);
					query = query + " AND A.loca_id IN (" + filtroLocalidade + ")";
				}

				query = query
						+ " UNION ALL"
						+ " SELECT cons_data, A.*, C.eeat_id, C.eeat_nome, 3 AS unop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
						+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade"
						+ " FROM cadastro.gerencia_regional A "
						+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
						+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
						+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
						+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) A "
						+ "INNER JOIN operacao.consumoeat B ON A.greg_id = B.greg_id AND A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
						+ "INNER JOIN operacao.eeat C ON B.eat_id = C.eeat_id" + " WHERE B.cons_data BETWEEN '" + dataIni + "' AND '" + dataFim + "'";

				// Se Perfil de Gerente, acesso a gerência Regional
				if (usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE) {
					query = query + " AND A.greg_id = " + usuarioProxy.getRegionalProxy().getCodigo();
				} else if (usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR) {
					for (LocalidadeProxy colunas : usuarioProxy.getLocalidadeProxy()) {
						filtroLocalidade = filtroLocalidade + colunas.getCodigo() + ",";
					}
					filtroLocalidade = filtroLocalidade.substring(0, filtroLocalidade.length() - 1);
					query = query + " AND A.loca_id IN (" + filtroLocalidade + ")";
				}

				query = query + " ORDER BY cons_data";
				List<List> valores = selectRegistros(query);

				if (dtFim.compareTo(new Date()) >= 0) {
					dtFim = new Date();
					gc.setTime(new Date());
					gc.add(Calendar.DATE, diasPendencia * -1);
					dtFim = gc.getTime();
				}

				List<LancamentoPendente> listaCompleta = new ArrayList<LancamentoPendente>();
				// GERAR LISTA COMPLETA DE CONSUMO ETA
				List<ConsumoETA> listaConsumoETA = getListaConsumoETAUsuario(usuarioProxy);
				gc.setTime(dtIni); // Inicializa com primeiro dia do mês
				while (dtFim.compareTo(gc.getTime()) >= 0) {// LAÇO DO PRIMEIRO
															// AO ÚLTIMO DIA DO
															// MÊS
					for (ConsumoETA lancamento : listaConsumoETA) {
						LancamentoPendente lancamentoAux = new LancamentoPendente();
						lancamentoAux.setRegionalProxy(lancamento.getRegionalProxy());
						lancamentoAux.setUnidadeNegocioProxy(lancamento.getUnidadeNegocioProxy());
						lancamentoAux.setMunicipioProxy(lancamento.getMunicipioProxy());
						lancamentoAux.setLocalidadeProxy(lancamento.getLocalidadeProxy());
						lancamentoAux.setTipoUnidadeOperacional(2);
						lancamentoAux.setCodigoUnidadeOperacional(lancamento.getEta().getCodigo());
						lancamentoAux.setUnidadeOperacional(lancamento.getEta().getDescricao());
						lancamentoAux.setDataConsumo(gc.getTime());
						listaCompleta.add(lancamentoAux);
					}
					gc.add(Calendar.DATE, 1); // INCREMENTA EM 1 DIA O LAÇO
				}

				// GERAR LISTA COMPLETA DE CONSUMO EAT
				List<ConsumoEAT> listaConsumoEAT = getListaConsumoEATUsuario(usuarioProxy);
				gc.setTime(dtIni); // Inicializa com primeiro dia do mês
				while (dtFim.compareTo(gc.getTime()) >= 0) {// LAÇO DO PRIMEIRO
															// AO ÚLTIMO DIA DO
															// MÊS
					for (ConsumoEAT lancamento : listaConsumoEAT) {
						LancamentoPendente lancamentoAux = new LancamentoPendente();
						lancamentoAux.setRegionalProxy(lancamento.getRegionalProxy());
						lancamentoAux.setUnidadeNegocioProxy(lancamento.getUnidadeNegocioProxy());
						lancamentoAux.setMunicipioProxy(lancamento.getMunicipioProxy());
						lancamentoAux.setLocalidadeProxy(lancamento.getLocalidadeProxy());
						lancamentoAux.setTipoUnidadeOperacional(3);
						lancamentoAux.setCodigoUnidadeOperacional(lancamento.getEat().getCodigo());
						lancamentoAux.setUnidadeOperacional(lancamento.getEat().getDescricao());
						lancamentoAux.setDataConsumo(gc.getTime());
						listaCompleta.add(lancamentoAux);
					}
					gc.add(Calendar.DATE, 1); // INCREMENTA EM 1 DIA O LAÇO
				}

				// GERANDO LISTA DE PENDÊNCIA
				Boolean blnPendente = true;
				for (LancamentoPendente consumo : listaCompleta) {
					for (List colunas : valores) {
						formataData = new SimpleDateFormat("yyyy-MM-dd");
						Date datConsumo = formataData.parse(colunas.get(0).toString());
						formataData = new SimpleDateFormat("yyyyMMdd");
						Integer dataLanc = Integer.parseInt(formataData.format(datConsumo));
						Integer dataCons = Integer.parseInt(formataData.format(consumo.getDataConsumo()));

						if (consumo.getRegionalProxy().getCodigo() == Integer.parseInt(colunas.get(1).toString())
								&& consumo.getUnidadeNegocioProxy().getCodigo() == Integer.parseInt(colunas.get(3).toString())
								&& consumo.getMunicipioProxy().getCodigo() == Integer.parseInt(colunas.get(5).toString())
								&& consumo.getLocalidadeProxy().getCodigo() == Integer.parseInt(colunas.get(7).toString())
								&& consumo.getCodigoUnidadeOperacional() == Integer.parseInt(colunas.get(9).toString())
								&& consumo.getTipoUnidadeOperacional() == Integer.parseInt(colunas.get(11).toString()) && dataCons.equals(dataLanc)) {
							blnPendente = false;
							break;
						}
					}
					if (blnPendente) {
						pendencias.add(consumo);
						if (tipoConsulta == 1)
							break;
					}
					blnPendente = true;
				}
			}
		}
		return (pendencias);
	}

	public List<LancamentoPendente> getVolumePendenteUsuario(UsuarioProxy usuarioProxy, Integer tipoConsulta) throws Exception {

		List<LancamentoPendente> pendencias = new ArrayList<LancamentoPendente>();
		String filtroLocalidade = "";

		// Recuperando Dias Pendencias
		Integer diasPendencia = usuarioProxy.getDiaPendencia();
		if (diasPendencia != 0) {// Controle de Pendências Desabilitado
			if (usuarioProxy.getPerfil() == PerfilBeanEnum.ADMIN || usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE
					|| usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR || usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR) {

				SimpleDateFormat formataData = new SimpleDateFormat("yyyyMMdd");
				GregorianCalendar gc = new GregorianCalendar();
				// Recuperando Mês de Referência Atual
				String referencia = usuarioProxy.getReferencia().toString();
				// Recuperando Primeiro Dia do Mês
				gc.set(Integer.parseInt(referencia.substring(0, 4)), Integer.parseInt(referencia.substring(4, 6)) - 1, 1, 0, 0, 0);

				Date dtIni = gc.getTime();
				String dataIni = formataData.format(dtIni);

				// EAB
				String query = "SELECT eabv_referencia, A.*, D.eeab_id, D.eeab_nome, ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
						+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade"
						+ " FROM cadastro.gerencia_regional A "
						+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
						+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
						+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
						+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) A "
						+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
						+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
						+ "INNER JOIN operacao.eeab D ON C.ucop_idoperacional = D.eeab_id "
						+ "INNER JOIN operacao.eeab_volume E ON D.eeab_id = E.eeab_id "
						+ "WHERE C.ucop_tipooperacional = 1" + "  AND E.eabv_referencia = '" + dataIni + "'";

				// Se Perfil de Gerente, acesso a gerência Regional
				if (usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE) {
					query = query + " AND A.greg_id = " + usuarioProxy.getRegionalProxy().getCodigo();
				} else if (usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR) {
					for (LocalidadeProxy colunas : usuarioProxy.getLocalidadeProxy()) {
						filtroLocalidade = filtroLocalidade + colunas.getCodigo() + ",";
					}
					filtroLocalidade = filtroLocalidade.substring(0, filtroLocalidade.length() - 1);
					query = query + " AND A.loca_id IN (" + filtroLocalidade + ")";
				}

				// ETA
				query = query
						+ " UNION ALL "
						+ "SELECT etav_referencia, A.*, D.eta_id, D.eta_nome, ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
						+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade" + " FROM cadastro.gerencia_regional A "
						+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
						+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
						+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id " + "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) A "
						+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
						+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
						+ "INNER JOIN operacao.eta D ON C.ucop_idoperacional = D.eta_id " + "INNER JOIN operacao.eta_volume E ON D.eta_id = E.eta_id "
						+ "WHERE C.ucop_tipooperacional = 2" + "  AND E.etav_referencia = '" + dataIni + "'";

				// Se Perfil de Gerente, acesso a gerência Regional
				if (usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE) {
					query = query + " AND A.greg_id = " + usuarioProxy.getRegionalProxy().getCodigo();
				} else if (usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR) {
					for (LocalidadeProxy colunas : usuarioProxy.getLocalidadeProxy()) {
						filtroLocalidade = filtroLocalidade + colunas.getCodigo() + ",";
					}
					filtroLocalidade = filtroLocalidade.substring(0, filtroLocalidade.length() - 1);
					query = query + " AND A.loca_id IN (" + filtroLocalidade + ")";
				}

				// EAT
				query = query
						+ " UNION ALL "
						+ "SELECT eatv_referencia, A.*, D.eeat_id, D.eeat_nome, ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
						+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade" + " FROM cadastro.gerencia_regional A "
						+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
						+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
						+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id " + "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) A "
						+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
						+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
						+ "INNER JOIN operacao.eeat D ON C.ucop_idoperacional = D.eeat_id " + "INNER JOIN operacao.eeat_volume E ON D.eeat_id = E.eeat_id "
						+ "WHERE C.ucop_tipooperacional = 3" + "  AND E.eatv_referencia = '" + dataIni + "'";

				// Se Perfil de Gerente, acesso a gerência Regional
				if (usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE) {
					query = query + " AND A.greg_id = " + usuarioProxy.getRegionalProxy().getCodigo();
				} else if (usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR) {
					for (LocalidadeProxy colunas : usuarioProxy.getLocalidadeProxy()) {
						filtroLocalidade = filtroLocalidade + colunas.getCodigo() + ",";
					}
					filtroLocalidade = filtroLocalidade.substring(0, filtroLocalidade.length() - 1);
					query = query + " AND A.loca_id IN (" + filtroLocalidade + ")";
				}

				// RSO
				query = query
						+ " UNION ALL "
						+ "SELECT rsov_referencia, A.*, D.rso_id, D.rso_nome, ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
						+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade" + " FROM cadastro.gerencia_regional A "
						+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
						+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
						+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id " + "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) A "
						+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
						+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
						+ "INNER JOIN operacao.rso D ON C.ucop_idoperacional = D.rso_id " + "INNER JOIN operacao.rso_volume E ON D.rso_id = E.rso_id "
						+ "WHERE C.ucop_tipooperacional = 4" + "  AND E.rsov_referencia = '" + dataIni + "'";

				// Se Perfil de Gerente, acesso a gerência Regional
				if (usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE) {
					query = query + " AND A.greg_id = " + usuarioProxy.getRegionalProxy().getCodigo();
				} else if (usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR) {
					for (LocalidadeProxy colunas : usuarioProxy.getLocalidadeProxy()) {
						filtroLocalidade = filtroLocalidade + colunas.getCodigo() + ",";
					}
					filtroLocalidade = filtroLocalidade.substring(0, filtroLocalidade.length() - 1);
					query = query + " AND A.loca_id IN (" + filtroLocalidade + ")";
				}

				query = query + " ORDER BY eabv_referencia";
				List<List> valores = selectRegistros(query);

				List<LancamentoPendente> listaCompleta = new ArrayList<LancamentoPendente>();
				// GERAR LISTA COMPLETA DE EAB
				List<LancamentoPendente> listaConsumoEAB = getListaEABUsuario(usuarioProxy);
				for (LancamentoPendente lancamento : listaConsumoEAB) {
					lancamento.setDataConsumo(gc.getTime());
					listaCompleta.add(lancamento);
				}

				// GERAR LISTA COMPLETA DE ETA
				List<LancamentoPendente> listaConsumoETA = getListaETAUsuario(usuarioProxy);
				for (LancamentoPendente lancamento : listaConsumoETA) {
					lancamento.setDataConsumo(gc.getTime());
					listaCompleta.add(lancamento);
				}

				// GERAR LISTA COMPLETA DE EAT
				List<LancamentoPendente> listaConsumoEAT = getListaEATUsuario(usuarioProxy);
				for (LancamentoPendente lancamento : listaConsumoEAT) {
					lancamento.setDataConsumo(gc.getTime());
					listaCompleta.add(lancamento);
				}

				// GERAR LISTA COMPLETA DE RSO
				List<LancamentoPendente> listaConsumoRSO = getListaRSOUsuario(usuarioProxy);
				for (LancamentoPendente lancamento : listaConsumoRSO) {
					lancamento.setDataConsumo(gc.getTime());
					listaCompleta.add(lancamento);
				}

				// GERANDO LISTA DE PENDÊNCIA
				Boolean blnPendente = true;
				for (LancamentoPendente consumo : listaCompleta) {
					for (List colunas : valores) {
						formataData = new SimpleDateFormat("yyyy-MM-dd");
						Date datConsumo = formataData.parse(colunas.get(0).toString());
						formataData = new SimpleDateFormat("yyyyMMdd");
						Integer dataLanc = Integer.parseInt(formataData.format(datConsumo));
						Integer dataCons = Integer.parseInt(formataData.format(consumo.getDataConsumo()));

						if (consumo.getRegionalProxy().getCodigo() == Integer.parseInt(colunas.get(1).toString())
								&& consumo.getUnidadeNegocioProxy().getCodigo() == Integer.parseInt(colunas.get(3).toString())
								&& consumo.getMunicipioProxy().getCodigo() == Integer.parseInt(colunas.get(5).toString())
								&& consumo.getLocalidadeProxy().getCodigo() == Integer.parseInt(colunas.get(7).toString())
								&& consumo.getCodigoUnidadeOperacional() == Integer.parseInt(colunas.get(9).toString())
								&& consumo.getTipoUnidadeOperacional() == Integer.parseInt(colunas.get(11).toString()) && dataCons.equals(dataLanc)) {
							blnPendente = false;
							break;
						}
					}
					if (blnPendente) {
						pendencias.add(consumo);
						if (tipoConsulta == 1)
							break;
					}
					blnPendente = true;
				}
			}
		}
		return (pendencias);
	}

	public List<LancamentoPendente> getHorasPendenteUsuario(UsuarioProxy usuarioProxy, Integer tipoConsulta) throws Exception {
		List<LancamentoPendente> pendencias = new ArrayList<LancamentoPendente>();
		String filtroLocalidade = "";

		// Recuperando Dias Pendencias
		Integer diasPendencia = usuarioProxy.getDiaPendencia();
		if (diasPendencia != 0) {// Controle de Pendências Desabilitado
			if (usuarioProxy.getPerfil() == PerfilBeanEnum.ADMIN || usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE
					|| usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR || usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR) {

				SimpleDateFormat formataData = new SimpleDateFormat("yyyyMMdd");
				GregorianCalendar gc = new GregorianCalendar();
				// Recuperando Mês de Referência Atual
				String referencia = usuarioProxy.getReferencia().toString();
				// Recuperando Primeiro Dia do Mês
				gc.set(Integer.parseInt(referencia.substring(0, 4)), Integer.parseInt(referencia.substring(4, 6)) - 1, 1, 0, 0, 0);

				Date dtIni = gc.getTime();
				String dataIni = formataData.format(dtIni);

				// EAB
				String query = "SELECT eabh_referencia, A.*, D.eeab_id, D.eeab_nome, ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
						+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade"
						+ " FROM cadastro.gerencia_regional A "
						+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
						+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
						+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
						+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) A "
						+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
						+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
						+ "INNER JOIN operacao.eeab D ON C.ucop_idoperacional = D.eeab_id "
						+ "INNER JOIN operacao.eeab_horas E ON D.eeab_id = E.eeab_id "
						+ "WHERE C.ucop_tipooperacional = 1" + "  AND E.eabh_referencia = '" + dataIni + "'";

				// Se Perfil de Gerente, acesso a gerência Regional
				if (usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE) {
					query = query + " AND A.greg_id = " + usuarioProxy.getRegionalProxy().getCodigo();
				} else if (usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR) {
					for (LocalidadeProxy colunas : usuarioProxy.getLocalidadeProxy()) {
						filtroLocalidade = filtroLocalidade + colunas.getCodigo() + ",";
					}
					filtroLocalidade = filtroLocalidade.substring(0, filtroLocalidade.length() - 1);
					query = query + " AND A.loca_id IN (" + filtroLocalidade + ")";
				}

				// ETA
				query = query
						+ " UNION ALL "
						+ "SELECT etah_referencia, A.*, D.eta_id, D.eta_nome, ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
						+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade" + " FROM cadastro.gerencia_regional A "
						+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
						+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
						+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id " + "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) A "
						+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
						+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
						+ "INNER JOIN operacao.eta D ON C.ucop_idoperacional = D.eta_id " + "INNER JOIN operacao.eta_horas E ON D.eta_id = E.eta_id "
						+ "WHERE C.ucop_tipooperacional = 2" + "  AND E.etah_referencia = '" + dataIni + "'";

				// Se Perfil de Gerente, acesso a gerência Regional
				if (usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE) {
					query = query + " AND A.greg_id = " + usuarioProxy.getRegionalProxy().getCodigo();
				} else if (usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR) {
					for (LocalidadeProxy colunas : usuarioProxy.getLocalidadeProxy()) {
						filtroLocalidade = filtroLocalidade + colunas.getCodigo() + ",";
					}
					filtroLocalidade = filtroLocalidade.substring(0, filtroLocalidade.length() - 1);
					query = query + " AND A.loca_id IN (" + filtroLocalidade + ")";
				}

				// EAT
				query = query
						+ " UNION ALL "
						+ "SELECT eath_referencia, A.*, D.eeat_id, D.eeat_nome, ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
						+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade" + " FROM cadastro.gerencia_regional A "
						+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
						+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
						+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id " + "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) A "
						+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
						+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
						+ "INNER JOIN operacao.eeat D ON C.ucop_idoperacional = D.eeat_id " + "INNER JOIN operacao.eeat_horas E ON D.eeat_id = E.eeat_id "
						+ "WHERE C.ucop_tipooperacional = 3" + "  AND E.eath_referencia = '" + dataIni + "'";

				// Se Perfil de Gerente, acesso a gerência Regional
				if (usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE) {
					query = query + " AND A.greg_id = " + usuarioProxy.getRegionalProxy().getCodigo();
				} else if (usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR) {
					for (LocalidadeProxy colunas : usuarioProxy.getLocalidadeProxy()) {
						filtroLocalidade = filtroLocalidade + colunas.getCodigo() + ",";
					}
					filtroLocalidade = filtroLocalidade.substring(0, filtroLocalidade.length() - 1);
					query = query + " AND A.loca_id IN (" + filtroLocalidade + ")";
				}

				// RSO
				query = query
						+ " UNION ALL "
						+ "SELECT rsoh_referencia, A.*, D.rso_id, D.rso_nome, ucop_tipooperacional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
						+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade" + " FROM cadastro.gerencia_regional A "
						+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
						+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
						+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id " + "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id) A "
						+ "INNER JOIN operacao.unidade_consumidora B ON A.uneg_id = B.uneg_id AND A.muni_id = B.muni_id AND A.loca_id = B.loca_id "
						+ "INNER JOIN operacao.unidade_consumidora_operacional C ON B.ucon_id = C.ucon_id "
						+ "INNER JOIN operacao.rso D ON C.ucop_idoperacional = D.rso_id " + "INNER JOIN operacao.rso_horas E ON D.rso_id = E.rso_id "
						+ "WHERE C.ucop_tipooperacional = 4" + "  AND E.rsoh_referencia = '" + dataIni + "'";

				// Se Perfil de Gerente, acesso a gerência Regional
				if (usuarioProxy.getPerfil() == PerfilBeanEnum.GERENTE) {
					query = query + " AND A.greg_id = " + usuarioProxy.getRegionalProxy().getCodigo();
				} else if (usuarioProxy.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuarioProxy.getPerfil() == PerfilBeanEnum.COORDENADOR) {
					for (LocalidadeProxy colunas : usuarioProxy.getLocalidadeProxy()) {
						filtroLocalidade = filtroLocalidade + colunas.getCodigo() + ",";
					}
					filtroLocalidade = filtroLocalidade.substring(0, filtroLocalidade.length() - 1);
					query = query + " AND A.loca_id IN (" + filtroLocalidade + ")";
				}

				query = query + " ORDER BY eabh_referencia";
				List<List> valores = selectRegistros(query);

				List<LancamentoPendente> listaCompleta = new ArrayList<LancamentoPendente>();
				// GERAR LISTA COMPLETA DE EAB
				List<LancamentoPendente> listaConsumoEAB = getListaEABUsuario(usuarioProxy);
				for (LancamentoPendente lancamento : listaConsumoEAB) {
					lancamento.setDataConsumo(gc.getTime());
					listaCompleta.add(lancamento);
				}

				// GERAR LISTA COMPLETA DE ETA
				List<LancamentoPendente> listaConsumoETA = getListaETAUsuario(usuarioProxy);
				for (LancamentoPendente lancamento : listaConsumoETA) {
					lancamento.setDataConsumo(gc.getTime());
					listaCompleta.add(lancamento);
				}

				// GERAR LISTA COMPLETA DE EAT
				List<LancamentoPendente> listaConsumoEAT = getListaEATUsuario(usuarioProxy);
				for (LancamentoPendente lancamento : listaConsumoEAT) {
					lancamento.setDataConsumo(gc.getTime());
					listaCompleta.add(lancamento);
				}

				// GERAR LISTA COMPLETA DE RSO
				List<LancamentoPendente> listaConsumoRSO = getListaRSOUsuario(usuarioProxy);
				for (LancamentoPendente lancamento : listaConsumoRSO) {
					lancamento.setDataConsumo(gc.getTime());
					listaCompleta.add(lancamento);
				}

				// GERANDO LISTA DE PENDÊNCIA
				Boolean blnPendente = true;
				for (LancamentoPendente consumo : listaCompleta) {
					for (List colunas : valores) {
						formataData = new SimpleDateFormat("yyyy-MM-dd");
						Date datConsumo = formataData.parse(colunas.get(0).toString());
						formataData = new SimpleDateFormat("yyyyMMdd");
						Integer dataLanc = Integer.parseInt(formataData.format(datConsumo));
						Integer dataCons = Integer.parseInt(formataData.format(consumo.getDataConsumo()));

						if (consumo.getRegionalProxy().getCodigo() == Integer.parseInt(colunas.get(1).toString())
								&& consumo.getUnidadeNegocioProxy().getCodigo() == Integer.parseInt(colunas.get(3).toString())
								&& consumo.getMunicipioProxy().getCodigo() == Integer.parseInt(colunas.get(5).toString())
								&& consumo.getLocalidadeProxy().getCodigo() == Integer.parseInt(colunas.get(7).toString())
								&& consumo.getCodigoUnidadeOperacional() == Integer.parseInt(colunas.get(9).toString())
								&& consumo.getTipoUnidadeOperacional() == Integer.parseInt(colunas.get(11).toString()) && dataCons.equals(dataLanc)) {
							blnPendente = false;
							break;
						}
					}
					if (blnPendente) {
						pendencias.add(consumo);
						if (tipoConsulta == 1)
							break;
					}
					blnPendente = true;
				}
			}
		}
		return (pendencias);
	}
}
