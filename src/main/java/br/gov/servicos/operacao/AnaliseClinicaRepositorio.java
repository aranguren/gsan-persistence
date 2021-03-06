package br.gov.servicos.operacao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.gov.model.operacao.AnaliseClinica;
import br.gov.model.operacao.LocalidadeProxy;
import br.gov.model.operacao.MunicipioProxy;
import br.gov.model.operacao.RegionalProxy;
import br.gov.model.operacao.UnidadeNegocioProxy;
import br.gov.model.util.GenericRepository;

@Stateless
public class AnaliseClinicaRepositorio extends GenericRepository<Integer, AnaliseClinica>{

	public List<AnaliseClinica> obterLista(){
		TypedQuery<AnaliseClinica> query = entity.createQuery("select c1 from AnaliseClinica c1 order by c1.referencia DESC", AnaliseClinica.class);
		return query.getResultList();
	}

	public AnaliseClinica obterAnaliseClinica(Integer codigo) throws Exception {
		TypedQuery<AnaliseClinica> query = entity.createQuery("select c1 from AnaliseClinica c1 where etev_id = " + codigo, AnaliseClinica.class);
		AnaliseClinica etevolume = query.getSingleResult();
		return etevolume;
	}

	public boolean verificaMesReferencia(Integer codigoRegional, Integer codigoUnidadeNegocio, Integer codigoMunicipio, Integer codigoLocalidade,
			String mesReferencia) throws Exception {
		TypedQuery<AnaliseClinica> query = entity.createQuery("select c1 from AnaliseClinica c1 where regionalProxy = " + codigoRegional
				+ " and unidadeNegocioProxy = " + codigoUnidadeNegocio + " and municipioProxy = " + codigoMunicipio + " and localidadeProxy = "
				+ codigoLocalidade + " and referencia = '" + mesReferencia + "'", AnaliseClinica.class);

		List<AnaliseClinica> analiseClinica = query.getResultList();
		if (analiseClinica.size() > 0) { // Se houver mes cadastrado
			return false;
		} else {
			return true;
		}
	}

	public List<AnaliseClinica> obterListaLazy(int startingAt, int maxPerPage, Map<String, Object> filters) throws Exception {
		CriteriaBuilder cb = entity.getCriteriaBuilder();
		CriteriaQuery<AnaliseClinica> q = cb.createQuery(AnaliseClinica.class);
		Root<AnaliseClinica> c = q.from(AnaliseClinica.class);
		Join<AnaliseClinica, RegionalProxy> greg = c.join("regionalProxy");
		Join<AnaliseClinica, UnidadeNegocioProxy> uneg = c.join("unidadeNegocioProxy");
		Join<AnaliseClinica, MunicipioProxy> muni = c.join("municipioProxy");
		Join<AnaliseClinica, LocalidadeProxy> loca = c.join("localidadeProxy");
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
					else
						path = c.get(key);
					if (key.equals("referencia")) {
						SimpleDateFormat formataData = new SimpleDateFormat("MM/yyyy");
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
		q.orderBy(cb.desc(c.get("referencia")));

		TypedQuery<AnaliseClinica> query = entity.createQuery(q);
		query.setMaxResults(maxPerPage);
		query.setFirstResult(startingAt);
		List<AnaliseClinica> lista = query.getResultList();

		return lista;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int obterQtdRegistros(Map<String, Object> filters) throws Exception {
		CriteriaBuilder cb = entity.getCriteriaBuilder();
		CriteriaQuery q = cb.createQuery(AnaliseClinica.class);
		Root<AnaliseClinica> c = q.from(AnaliseClinica.class);
		Join<AnaliseClinica, RegionalProxy> greg = c.join("regionalProxy");
		Join<AnaliseClinica, UnidadeNegocioProxy> uneg = c.join("unidadeNegocioProxy");
		Join<AnaliseClinica, MunicipioProxy> muni = c.join("municipioProxy");
		Join<AnaliseClinica, LocalidadeProxy> loca = c.join("localidadeProxy");
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
					else
						path = c.get(key);
					if (key.equals("referencia")) {
						SimpleDateFormat formataData = new SimpleDateFormat("MM/yyyy");
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

}
