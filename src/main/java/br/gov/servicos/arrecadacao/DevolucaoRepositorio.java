package br.gov.servicos.arrecadacao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.model.arrecadacao.Devolucao;
import br.gov.model.faturamento.CreditoRealizar;

@Stateless
public class DevolucaoRepositorio {

	@PersistenceContext
	private EntityManager entity;
	
	public boolean existeCreditoComDevolucao(Collection<CreditoRealizar> creditosRealizar) {
		return !buscarDevolucaoPorCreditoRealizar(creditosRealizar).isEmpty();
	}
	
	public Collection<Devolucao> buscarDevolucaoPorCreditoRealizar(Collection<CreditoRealizar> creditosRealizar){
		List<Integer> ids = new ArrayList<Integer>();

		creditosRealizar.forEach( cr -> ids.add(cr.getId()));
		
		Collection<Devolucao> resultado = entity.createQuery("select devolucao from Devolucao as devolucao inner join devolucao.creditoRealizar crar "
																+ "where crar.id in (:ids)", Devolucao.class)
												.setParameter("ids", ids)
												.getResultList();
		
		return resultado;
	}
}
