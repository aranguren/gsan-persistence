package br.gov.servicos.faturamento;

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.financeiro.TipoFinanciamento;
import br.gov.servicos.to.DebitoCobradoNaoParceladoTO;
import br.gov.servicos.to.ParcelaDebitoCobradoTO;

@Stateless
public class DebitoCobradoRepositorio {

	@PersistenceContext
	private EntityManager entity;
	
	public List<Integer> debitosCobradosDasContas(List<Integer> ids){
		StringBuilder sql = new StringBuilder();
		sql.append("select id from DebitoCobrado as debitoCobrado where debitoCobrado.conta.id in (:ids) ");
		
		List<Integer> lista = entity.createQuery(sql.toString(), Integer.class)
									.setParameter("ids", ids)
									.getResultList();
		
		return lista;		
	}
	
	public void apagarDebitosCobradosDasContas(List<Integer> ids){
		String delete = "delete from faturamento.debito_cobrado where cnta_id in (:ids)";
		
		entity.createNativeQuery(delete)
		.setParameter("ids", ids)
		.executeUpdate();
	}

	public Integer inserir(DebitoCobrado debitoCobrado) {
		entity.persist(debitoCobrado);
		entity.flush();
		
		return debitoCobrado.getId();
	}		
	
	public Collection<ParcelaDebitoCobradoTO> pesquisarDebitoCobradoParcelamento(Integer idConta) {

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT new br.gov.servicos.to.ParcelaDebitoCobradoTO(")
        .append("  dbcb.numeroPrestacaoDebito, ")
        .append("  (dbcb.numeroPrestacao - COALESCE(dbcb.numeroParcelaBonus,0)) as totalParcela, ")
        .append("  SUM(dbcb.valorPrestacao) as totalPrestacao, ")
        .append("  dbcb.debitoTipo.codigoConstante) ")
        .append("FROM ")
        .append("  DebitoCobrado dbcb ")
        .append("  INNER JOIN dbcb.conta conta ")
        .append("WHERE ")
        .append("  conta.id = :idConta AND ")
        .append("  dbcb.tipoFinanciamento IN (:agua, :esgoto, :servico) ")
        .append("GROUP BY 1, 2, 4");

		return entity.createQuery(sql.toString(), ParcelaDebitoCobradoTO.class)
						.setParameter("idConta", idConta)
						.setParameter("agua", TipoFinanciamento.PARCELAMENTO_AGUA.getId())
						.setParameter("esgoto", TipoFinanciamento.PARCELAMENTO_ESGOTO.getId())
						.setParameter("servico", TipoFinanciamento.PARCELAMENTO_SERVICO.getId())
						.getResultList();
	}
	
	public Collection<DebitoCobradoNaoParceladoTO> pesquisarDebitoCobradoSemParcelamento(Integer idConta) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT new br.gov.servicos.to.DebitoCobradoNaoParceladoTO(")
		.append("dbcb.anoMesReferenciaDebito ")
		.append(", dbcb.numeroPrestacaoDebito ")
		.append(", dbcb.numeroPrestacao - COALESCE(dbcb.numeroParcelaBonus,0) ")
		.append(", dbcb.valorPrestacao ")
		.append(", dbtp.id ")
		.append(", dbtp.descricao ")
		.append(", dbtp.codigoConstante ")
		.append(")")
		.append("FROM DebitoCobrado dbcb ")
		.append("INNER JOIN dbcb.conta conta ")
		.append("INNER JOIN dbcb.debitoTipo dbtp ")
		.append("WHERE conta.id = :idConta ")
		.append("AND dbcb.tipoFinanciamento NOT IN (:agua, :esgoto, :servico) ")
		.append("ORDER BY dbtp.id, dbcb.anoMesReferenciaDebito ");

		return entity.createQuery(sql.toString(), DebitoCobradoNaoParceladoTO.class)
    			.setParameter("idConta", idConta)
    			.setParameter("agua", TipoFinanciamento.PARCELAMENTO_AGUA.getId())
    			.setParameter("esgoto", TipoFinanciamento.PARCELAMENTO_ESGOTO.getId())
    			.setParameter("servico", TipoFinanciamento.PARCELAMENTO_SERVICO.getId())
    			.getResultList();
	}
}
