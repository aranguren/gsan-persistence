package br.gov.servicos.operacao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.gov.model.operacao.ConsumoEAT;
import br.gov.model.operacao.ConsumoEATProduto;
import br.gov.model.operacao.EEAT;
import br.gov.model.operacao.LocalidadeProxy;
import br.gov.model.operacao.MunicipioProxy;
import br.gov.model.operacao.PerfilBeanEnum;
import br.gov.model.operacao.Produto;
import br.gov.model.operacao.RegionalProxy;
import br.gov.model.operacao.UnidadeNegocioProxy;
import br.gov.model.operacao.UsuarioProxy;
import br.gov.model.util.GenericRepository;

@Stateless
public class ConsumoEATRepositorio extends GenericRepository<Integer, ConsumoEAT>{

	@EJB
	private ProxyOperacionalRepositorio fachadaProxy;

	@EJB
	private ProdutoRepositorio fachadaProduto;

	public void atualizarConsumoProduto(ConsumoEATProduto obj) throws Exception {
		entity.merge(obj);
	}

	public List<ConsumoEAT> obterLista() {
		TypedQuery<ConsumoEAT> query = entity.createQuery("select c1 from ConsumoEAT c1 order by cons_data desc", ConsumoEAT.class);
		List<ConsumoEAT> lista = query.getResultList();

		return lista;
	}

	public ConsumoEAT obterConsumo(Integer codigo) throws Exception {
		TypedQuery<ConsumoEAT> query = entity.createQuery("select c1 from ConsumoEAT c1 where cons_id = " + codigo, ConsumoEAT.class);
		ConsumoEAT consumoEAT = query.getSingleResult();

		ConsumoEAT ConsumoEAT = consumoEAT;
		for (int j = 0; j < ConsumoEAT.getConsumoProduto().size(); j++) {
			consumoEAT.getConsumoProduto().get(j);
			consumoEAT.getConsumoProduto().get(j).setProduto(fachadaProduto.obterProduto(consumoEAT.getConsumoProduto().get(j).getProduto().getCodigo()));
		}
		return consumoEAT;
	}

	public ConsumoEAT obterConsumoLazy(Integer codigo) throws Exception {
		TypedQuery<ConsumoEAT> query = entity.createQuery("select c1 from ConsumoEAT c1 where cons_id = " + codigo, ConsumoEAT.class);
		ConsumoEAT consumoEAT = query.getSingleResult();
		return consumoEAT;
	}

	public List<ConsumoEAT> obterListaLazy(int startingAt, int maxPerPage, Map<String, Object> filters) throws Exception {
		CriteriaBuilder cb = entity.getCriteriaBuilder();
		CriteriaQuery<ConsumoEAT> q = cb.createQuery(ConsumoEAT.class);
		Root<ConsumoEAT> c = q.from(ConsumoEAT.class);
		Join<ConsumoEAT, RegionalProxy> greg = c.join("regionalProxy");
		Join<ConsumoEAT, UnidadeNegocioProxy> uneg = c.join("unidadeNegocioProxy");
		Join<ConsumoEAT, MunicipioProxy> muni = c.join("municipioProxy");
		Join<ConsumoEAT, LocalidadeProxy> loca = c.join("localidadeProxy");
		Join<ConsumoEAT, EEAT> EAT = c.join("eat");
		q.select(c);
		if (filters != null && !filters.isEmpty()) {
			Predicate[] predicates = new Predicate[filters.size()];
			int i = 0;
			for (Map.Entry<String, Object> entry : filters.entrySet()) {
				String key = entry.getKey();
				String val = entry.getValue().toString();
				Expression<String> path;
				try {
					if (key.equals("regionalProxy.nome"))
						path = greg.get("nome");
					else if (key.equals("unidadeNegocioProxy.nome"))
						path = uneg.get("nome");
					else if (key.equals("municipioProxy.nome"))
						path = muni.get("nome");
					else if (key.equals("localidadeProxy.nome"))
						path = loca.get("nome");
					else if (key.equals("eeat.descricao"))
						path = EAT.get("descricao");
					else
						path = c.get(key);
					if (key.equals("dataConsumo")) {
						SimpleDateFormat formataData = new SimpleDateFormat("dd/MM/yyyy");
						Date dataConsumo = formataData.parse(val);
						predicates[i] = cb.and(cb.equal(path, dataConsumo));
					} else {
						// if
						// (RegionalProxy.class.getDeclaredField(key).getType().equals(String.class))
						// {
						predicates[i] = cb.and(cb.like(cb.lower(path), "%" + val.toLowerCase() + "%"));
					}
				} catch (SecurityException ex) {
					ex.printStackTrace();
				}
				i++;
			}
			q.where(predicates);
		}
		q.orderBy(cb.desc(c.get("dataConsumo")));

		TypedQuery<ConsumoEAT> query = entity.createQuery(q);
		query.setMaxResults(maxPerPage);
		query.setFirstResult(startingAt);
		List<ConsumoEAT> lista = query.getResultList();

		return lista;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int obterQtdRegistros(Map<String, Object> filters) throws Exception {
		CriteriaBuilder cb = entity.getCriteriaBuilder();
		CriteriaQuery q = cb.createQuery(ConsumoEAT.class);
		Root<ConsumoEAT> c = q.from(ConsumoEAT.class);
		Join<ConsumoEAT, RegionalProxy> greg = c.join("regionalProxy");
		Join<ConsumoEAT, UnidadeNegocioProxy> uneg = c.join("unidadeNegocioProxy");
		Join<ConsumoEAT, MunicipioProxy> muni = c.join("municipioProxy");
		Join<ConsumoEAT, LocalidadeProxy> loca = c.join("localidadeProxy");
		Join<ConsumoEAT, EEAT> EAT = c.join("eat");
		q.select(cb.count(c));
		if (filters != null && !filters.isEmpty()) {
			Predicate[] predicates = new Predicate[filters.size()];
			int i = 0;
			for (Map.Entry<String, Object> entry : filters.entrySet()) {
				String key = entry.getKey();
				String val = entry.getValue().toString();
				Expression<String> path;
				try {
					if (key.equals("regionalProxy.nome"))
						path = greg.get("nome");
					else if (key.equals("unidadeNegocioProxy.nome"))
						path = uneg.get("nome");
					else if (key.equals("municipioProxy.nome"))
						path = muni.get("nome");
					else if (key.equals("localidadeProxy.nome"))
						path = loca.get("nome");
					else if (key.equals("eeat.descricao"))
						path = EAT.get("descricao");
					else
						path = c.get(key);
					if (key.equals("dataConsumo")) {
						SimpleDateFormat formataData = new SimpleDateFormat("dd/MM/yyyy");
						Date dataConsumo = formataData.parse(val);
						predicates[i] = cb.and(cb.equal(path, dataConsumo));
					} else {
						predicates[i] = cb.and(cb.like(cb.lower(path), "%" + val.toLowerCase() + "%"));
					}
				} catch (SecurityException ex) {
					ex.printStackTrace();
				}
				i++;
			}
			q.where(predicates);
		}
		Query query = entity.createQuery(q);
		return ((Long) query.getSingleResult()).intValue();
	}

	public List<Produto> listarProdutos() {
		TypedQuery<Produto> query = entity.createQuery("select c1 from Produto c1", Produto.class);
		return query.getResultList();
	}

	@SuppressWarnings("rawtypes")
	public List<RegionalProxy> getListaConsumoEATRegional(UsuarioProxy usuario) throws Exception {

		String localidade = "";
		String query = "SELECT DISTINCT A.greg_id, A.greg_nmregional FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade, G.eeat_id, G.eeat_nome "
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

		query = query + " ORDER BY A.greg_nmregional";

		List<List> valores = fachadaProxy.selectRegistros(query);
		List<RegionalProxy> lista = new ArrayList<RegionalProxy>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			RegionalProxy consumo = new RegionalProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString());
			lista.add(consumo);
		}

		return lista;
	}

	@SuppressWarnings("rawtypes")
	public List<UnidadeNegocioProxy> getListaConsumoEATUnidadeNegocio(UsuarioProxy usuario, Integer codigoRegional) throws Exception {

		String localidade = "";
		String query = "SELECT DISTINCT A.uneg_id, A.uneg_nmunidadenegocio FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade, G.eeat_id, G.eeat_nome "
				+ " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
				+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id "
				+ "INNER JOIN operacao.registroconsumoeat F ON A.greg_id = F.greg_id AND B.uneg_id = F.uneg_id AND C.loca_id = F.loca_id AND E.muni_id = F.muni_id "
				+ "INNER JOIN operacao.eeat G ON F.eat_id = G.eeat_id) AS A ";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " WHERE A.greg_id = " + usuario.getRegionalProxy().getCodigo() + "   AND A.greg_id = " + codigoRegional;
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " WHERE A.loca_id IN (" + localidade + ")" + "   AND A.greg_id = " + codigoRegional;
		} else {
			query = query + " WHERE A.greg_id = " + codigoRegional;
		}

		query = query + " ORDER BY A.uneg_nmunidadenegocio";

		List<List> valores = fachadaProxy.selectRegistros(query);
		List<UnidadeNegocioProxy> lista = new ArrayList<UnidadeNegocioProxy>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			UnidadeNegocioProxy consumo = new UnidadeNegocioProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString());
			lista.add(consumo);
		}

		return lista;
	}

	@SuppressWarnings("rawtypes")
	public List<MunicipioProxy> getListaConsumoEATMunicipio(UsuarioProxy usuario, Integer codigoRegional, Integer codigoUnidadeNegocio) throws Exception {

		String localidade = "";
		String query = "SELECT DISTINCT A.muni_id, A.muni_nmmunicipio FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade, G.eeat_id, G.eeat_nome "
				+ " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
				+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id "
				+ "INNER JOIN operacao.registroconsumoeat F ON A.greg_id = F.greg_id AND B.uneg_id = F.uneg_id AND C.loca_id = F.loca_id AND E.muni_id = F.muni_id "
				+ "INNER JOIN operacao.eeat G ON F.eat_id = G.eeat_id) AS A ";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " WHERE A.greg_id = " + usuario.getRegionalProxy().getCodigo() + "   AND A.greg_id = " + codigoRegional + "   AND A.uneg_id = "
					+ codigoUnidadeNegocio;
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " WHERE A.loca_id IN (" + localidade + ")" + "   AND A.greg_id = " + codigoRegional + "   AND A.uneg_id = " + codigoUnidadeNegocio;
		} else {
			query = query + " WHERE A.greg_id = " + codigoRegional + "   AND A.uneg_id = " + codigoUnidadeNegocio;
		}

		query = query + " ORDER BY A.muni_nmmunicipio";

		List<List> valores = fachadaProxy.selectRegistros(query);
		List<MunicipioProxy> lista = new ArrayList<MunicipioProxy>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			MunicipioProxy consumo = new MunicipioProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString());
			lista.add(consumo);
		}

		return lista;
	}

	@SuppressWarnings("rawtypes")
	public List<LocalidadeProxy> getListaConsumoEATLocalidade(UsuarioProxy usuario, Integer codigoRegional, Integer codigoUnidadeNegocio,
			Integer codigoMunicipio) throws Exception {

		String localidade = "";
		String query = "SELECT DISTINCT A.loca_id, A.loca_nmlocalidade FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade, G.eeat_id, G.eeat_nome "
				+ " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
				+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id "
				+ "INNER JOIN operacao.registroconsumoeat F ON A.greg_id = F.greg_id AND B.uneg_id = F.uneg_id AND C.loca_id = F.loca_id AND E.muni_id = F.muni_id "
				+ "INNER JOIN operacao.eeat G ON F.eat_id = G.eeat_id) AS A ";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " WHERE A.greg_id = " + usuario.getRegionalProxy().getCodigo() + "   AND A.greg_id = " + codigoRegional + "   AND A.uneg_id = "
					+ codigoUnidadeNegocio + "   AND A.muni_id = " + codigoMunicipio;
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " WHERE A.loca_id IN (" + localidade + ")" + "   AND A.greg_id = " + codigoRegional + "   AND A.uneg_id = " + codigoUnidadeNegocio
					+ "   AND A.muni_id = " + codigoMunicipio;
		} else {
			query = query + " WHERE A.greg_id = " + codigoRegional + "   AND A.uneg_id = " + codigoUnidadeNegocio + "   AND A.muni_id = " + codigoMunicipio;
		}
		query = query + " ORDER BY A.loca_nmlocalidade";

		List<List> valores = fachadaProxy.selectRegistros(query);
		List<LocalidadeProxy> lista = new ArrayList<LocalidadeProxy>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			LocalidadeProxy consumo = new LocalidadeProxy(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString());
			lista.add(consumo);
		}

		return lista;
	}

	@SuppressWarnings("rawtypes")
	public List<EEAT> getListaConsumoEAT(UsuarioProxy usuario, Integer codigoRegional, Integer codigoUnidadeNegocio, Integer codigoMunicipio,
			Integer codigoLocalidade) throws Exception {

		String localidade = "";
		String query = "SELECT DISTINCT A.eeat_id, A.eeat_nome FROM (SELECT DISTINCT A.greg_id, A.greg_nmregional, B.uneg_id, B.uneg_nmunidadenegocio,"
				+ " E.muni_id, E.muni_nmmunicipio, C.loca_id, C.loca_nmlocalidade, G.eeat_id, G.eeat_nome "
				+ " FROM cadastro.gerencia_regional A "
				+ "INNER JOIN cadastro.unidade_negocio B ON A.greg_id = B.greg_id "
				+ "INNER JOIN cadastro.localidade C ON A.greg_id = C.greg_id AND B.uneg_id = C.uneg_ID "
				+ "INNER JOIN cadastro.setor_comercial D ON C.loca_id = D.loca_id "
				+ "INNER JOIN cadastro.municipio E ON D.muni_id = E.muni_id "
				+ "INNER JOIN operacao.registroconsumoeat F ON A.greg_id = F.greg_id AND B.uneg_id = F.uneg_id AND C.loca_id = F.loca_id AND E.muni_id = F.muni_id "
				+ "INNER JOIN operacao.eeat G ON F.eat_id = G.eeat_id) AS A ";

		// Se Perfil de Gerente, acesso a gerência Regional
		if (usuario.getPerfil() == PerfilBeanEnum.GERENTE) {
			query = query + " WHERE A.greg_id = " + usuario.getRegionalProxy().getCodigo() + "   AND A.greg_id = " + codigoRegional + "   AND A.uneg_id = "
					+ codigoUnidadeNegocio + "   AND A.muni_id = " + codigoMunicipio + "   AND A.loca_id = " + codigoLocalidade;
		} else if (usuario.getPerfil() == PerfilBeanEnum.SUPERVISOR || usuario.getPerfil() == PerfilBeanEnum.COORDENADOR) {
			for (LocalidadeProxy colunas : usuario.getLocalidadeProxy()) {
				localidade = localidade + colunas.getCodigo() + ",";
			}
			localidade = localidade.substring(0, localidade.length() - 1);
			query = query + " WHERE A.loca_id IN (" + localidade + ")" + "   AND A.greg_id = " + codigoRegional + "   AND A.uneg_id = " + codigoUnidadeNegocio
					+ "   AND A.muni_id = " + codigoMunicipio + "   AND A.loca_id = " + codigoLocalidade;
		} else {
			query = query + " WHERE A.greg_id = " + codigoRegional + "   AND A.uneg_id = " + codigoUnidadeNegocio + "   AND A.muni_id = " + codigoMunicipio
					+ "   AND A.loca_id = " + codigoLocalidade;
		}
		query = query + " ORDER BY A.eeat_nome";

		List<List> valores = fachadaProxy.selectRegistros(query);
		List<EEAT> lista = new ArrayList<EEAT>();

		if (valores == null) {
			return lista;
		}

		for (List colunas : valores) {
			EEAT consumo = new EEAT(Integer.parseInt(colunas.get(0).toString()), colunas.get(1).toString());
			lista.add(consumo);
		}

		return lista;
	}
}
