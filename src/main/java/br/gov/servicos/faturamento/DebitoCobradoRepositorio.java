package br.gov.servicos.faturamento;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.model.faturamento.DebitoCobrado;

@Stateless
public class DebitoCobradoRepositorio {

	@PersistenceContext
	private EntityManager entity;
	
	public List<Long> debitosCobradosDasContas(List<Long> ids){
		StringBuilder sql = new StringBuilder();
		sql.append("select id from DebitoCobrado as debitoCobrado where debitoCobrado.conta.id in (:ids) ");
		
		List<Long> lista = entity.createQuery(sql.toString(), Long.class)
									.setParameter("ids", ids)
									.getResultList();
		
		return lista;		
	}
	
	public void apagarDebitosCobradosDasContas(List<Long> ids){
		String delete = "delete from faturamento.debito_cobrado where cnta_id in (:ids)";
		
		entity.createNativeQuery(delete)
		.setParameter("ids", ids)
		.executeUpdate();
	}

	public Long inserir(DebitoCobrado debitoCobrado) {
		entity.persist(debitoCobrado);
		entity.flush();
		
		return debitoCobrado.getId();
	}		
}
