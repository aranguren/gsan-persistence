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

import br.gov.model.operacao.ContratoEnergia;
import br.gov.model.operacao.UnidadeConsumidora;
import br.gov.model.util.GenericRepository;

@Stateless
public class ContratoEnergiaRepositorio extends GenericRepository<Integer, ContratoEnergia>{

	public List<ContratoEnergia> obterLista() {
		TypedQuery<ContratoEnergia> query = entity.createQuery("select c1 from ContratoEnergia c1 order by cene_dataini desc", ContratoEnergia.class);
		return query.getResultList();
	}

	public ContratoEnergia obterContrato(Integer codigo) throws Exception {
		TypedQuery<ContratoEnergia> query = entity.createQuery("select c1 from ContratoEnergia c1 where cene_id = " + codigo, ContratoEnergia.class);
		ContratoEnergia contratoEnergia = query.getSingleResult();

		ContratoEnergia ContratoEnergia = contratoEnergia;
		for (int j = 0; j < ContratoEnergia.getDemanda().size(); j++) {
			contratoEnergia.getDemanda().get(j);
		}
		return contratoEnergia;
	}

	public ContratoEnergia obterContratoVigente(Integer codigoUC) throws Exception {
		try {
			TypedQuery<ContratoEnergia> query = entity.createQuery(
					"select c1 from ContratoEnergia c1 where ucon_id = " + codigoUC + " order by c1.dataInicial", ContratoEnergia.class);
			query.setMaxResults(1);
			ContratoEnergia contratoEnergia = query.getSingleResult();
			return contratoEnergia;
		} catch (Exception e) {
			return null;
		}
	}

	public List<ContratoEnergia> obterListaLazy(int startingAt, int maxPerPage, Map<String, Object> filters) throws Exception {
		CriteriaBuilder cb = entity.getCriteriaBuilder();
		CriteriaQuery<ContratoEnergia> q = cb.createQuery(ContratoEnergia.class);
		Root<ContratoEnergia> c = q.from(ContratoEnergia.class);
		Join<ContratoEnergia, UnidadeConsumidora> ucon = c.join("unidadeConsumidora");
		q.select(c);
		if (filters != null && !filters.isEmpty()) {
			Predicate[] predicates = new Predicate[filters.size()];
			int i = 0;
			for (Map.Entry<String, Object> entry : filters.entrySet()) {
				String key = entry.getKey();
				String val = entry.getValue().toString();
				Expression<String> path;
				try {
					if (key.equals("unidadeConsumidora.uc")) {
						path = ucon.get("uc");
						predicates[i] = cb.and(cb.equal(path, val));
					} else if (key.equals("dataInicial") || key.equals("dataFinal")) {
						path = c.get(key);
						SimpleDateFormat formataData = new SimpleDateFormat("dd/MM/yyyy");
						val = "01/" + val;
						Date dataConsumo = formataData.parse(val);
						predicates[i] = cb.and(cb.equal(path, dataConsumo));
					} else {
						path = c.get(key);
						predicates[i] = cb.and(cb.like(cb.lower(path), "%" + val.toLowerCase() + "%"));
					}
				} catch (SecurityException ex) {
					ex.printStackTrace();
				}
				i++;
			}
			q.where(predicates);
		}
		q.orderBy(cb.desc(c.get("codigo")));
		TypedQuery<ContratoEnergia> query = entity.createQuery(q);
		query.setMaxResults(maxPerPage);
		query.setFirstResult(startingAt);
		List<ContratoEnergia> lista = query.getResultList();
		return lista;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int obterQtdRegistros(Map<String, Object> filters) throws Exception {
		CriteriaBuilder cb = entity.getCriteriaBuilder();
		CriteriaQuery q = cb.createQuery(ContratoEnergia.class);
		Root<ContratoEnergia> c = q.from(ContratoEnergia.class);
		Join<ContratoEnergia, UnidadeConsumidora> ucon = c.join("unidadeConsumidora");
		q.select(cb.count(c));
		if (filters != null && !filters.isEmpty()) {
			Predicate[] predicates = new Predicate[filters.size()];
			int i = 0;
			for (Map.Entry<String, Object> entry : filters.entrySet()) {
				String key = entry.getKey();
				String val = entry.getValue().toString();
				Expression<String> path;
				try {
					if (key.equals("unidadeConsumidora.uc")) {
						path = ucon.get("uc");
						predicates[i] = cb.and(cb.equal(path, val));
					} else if (key.equals("dataInicial") || key.equals("dataFinal")) {
						path = c.get(key);
						SimpleDateFormat formataData = new SimpleDateFormat("dd/MM/yyyy");
						val = "01/" + val;
						Date dataConsumo = formataData.parse(val);
						predicates[i] = cb.and(cb.equal(path, dataConsumo));
					} else {
						path = c.get(key);
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
