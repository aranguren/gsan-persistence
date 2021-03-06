package br.gov.servicos.operacao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;

import br.gov.model.operacao.MacroMedidor;
import br.gov.model.util.GenericRepository;

@Stateless
public class MacroMedidorRepositorio extends GenericRepository<Integer, MacroMedidor>{
	public List<MacroMedidor> obterLista() {
		TypedQuery<MacroMedidor> query = entity.createQuery("select c1 from MacroMedidor c1", MacroMedidor.class);
		List<MacroMedidor> lista = query.getResultList();

		return lista;
	}

	public List<MacroMedidor> obterLista2(Integer codigo) throws Exception {
		TypedQuery<MacroMedidor> query = entity.createQuery("select c1 from MacroMedidor c1 where mmed_id = " + codigo, MacroMedidor.class);
		List<MacroMedidor> lista = query.getResultList();

		return lista;
	}

	public MacroMedidor obterMacroMedidor(Integer codigo) throws Exception {
		TypedQuery<MacroMedidor> query = entity.createQuery("select c1 from MacroMedidor c1 where mmed_id = " + codigo, MacroMedidor.class);
		MacroMedidor medidor = query.getSingleResult();
		MacroMedidor MacroMedidor = medidor;
		for (int j = 0; j < MacroMedidor.getAfericao().size(); j++) {
			medidor.getAfericao().get(j);
		}
		return medidor;
	}

}
