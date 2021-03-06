package br.gov.servicos.micromedicao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.persistence.util.SingleDeployment;

@RunWith(Arquillian.class)
public class ConsumoHistoricoRepositorioTest extends SingleDeployment{

	@Inject
	private ConsumoHistoricoRepositorio repositorio;
	
	@Test
	@UsingDataSet("consumoHistorico.yml")
	public void obterMediaconsumo(){
		List<ConsumoHistorico> lista = repositorio.obterConsumoMedio(1, 201406, 201408, 1);
		
		ConsumoHistorico consumo = lista.get(0);
		
		assertEquals(30, consumo.getNumeroConsumoCalculoMedia().intValue());
	}
}
