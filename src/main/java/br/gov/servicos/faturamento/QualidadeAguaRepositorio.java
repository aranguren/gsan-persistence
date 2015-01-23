package br.gov.servicos.faturamento;

import javax.ejb.Stateless;

import br.gov.model.faturamento.QualidadeAgua;
import br.gov.model.util.GenericRepository;

@Stateless
public class QualidadeAguaRepositorio extends GenericRepository<Integer, QualidadeAgua>{
	
	public QualidadeAgua buscarPorAnoMesESistemaAbastecimentoComFonteCaptacaoETipoCaptacao(Integer anoMesReferencia, Integer idSistemaAbastecimento){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT qualidadeagua FROM QualidadeAgua qualidadeagua ")
		.append("INNER JOIN qualidadeagua.fonteCaptacao fontecaptacao ")
		.append("INNER JOIN fontecaptacao.tipoCaptacao tipocaptacao ")
		.append("where qualidadeagua.anoMesReferencia = :anoMesReferencia ")
		.append("AND qualidadeagua.sistemaAbastecimento.id = :idSistemaAbastecimento");

		return entity.createQuery(sql.toString(), QualidadeAgua.class)
		.setParameter("anoMesReferencia", anoMesReferencia)
		.setParameter("idSistemaAbastecimento", idSistemaAbastecimento).getSingleResult();
	}
	
	public QualidadeAgua buscarPorAnoMesELocalidadeESetorComFonteCaptacao(Integer anoMesReferencia,
			Integer idSetor, Integer idLocalidade){

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT qualidadeagua FROM QualidadeAgua qualidadeagua ")
		.append("LEFT JOIN qualidadeagua.localidade localidade ")
		.append("LEFT JOIN qualidadeagua.setorComercial setorcomercial ")
		.append("INNER JOIN qualidadeagua.sistemaAbastecimento sistemaabastecimento ")
		.append("where qualidadeagua.anoMesReferencia = :anoMesReferencia ")
		.append("AND localidade.id = :idLocalidade ")
		.append("AND setorcomercial.id = :idSetor");

		return entity.createQuery(sql.toString(), QualidadeAgua.class)
		.setParameter("anoMesReferencia", anoMesReferencia)
		.setParameter("idLocalidade", idLocalidade)
		.setParameter("idSetor", idSetor).getSingleResult();

		
//		QuerySelectBuilder sql = new QuerySelectBuilder(entity, QualidadeAgua.class);
//		sql.appendLeftJoin("localidade")
//		.appendInnerJoin("sistemaAbastecimento")
//		.appendLeftJoin("setorComercial")
//		.appendCondition("anoMesReferencia", anoMesReferencia)
//		.appendJoinCondition("localidade", "id", idLocalidade)
//		.appendJoinCondition("setorComercial", "id", idSetor);
//		
//		return (QualidadeAgua)sql.createSelectQuery().getSingleResult();
	}
}
