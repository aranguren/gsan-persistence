package br.gov.servicos.faturamento;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.persistence.util.SingleDeployment;


@RunWith(Arquillian.class)
public class DebitoCobrarRepositorioTest extends SingleDeployment{
	
	private static Logger logger = Logger.getLogger(DebitoCobrarRepositorioTest.class);
		
	@Inject
	DebitoCobrarRepositorio repositorio;
	
	@Inject
	ContaRepositorio contaRepositorio;
	
	@Test
	@UsingDataSet("debitosCobrarVigentes.yml")
	public void debitosCobrarVigentes() throws Exception {
		logger.info("Verificar debitos a cobrar vigentes");
		Imovel imovel = new Imovel();
		imovel.setId(1);

		Collection<DebitoCobrar> debitos = repositorio.debitosCobrarPorImovelComPendenciaESemRevisao(imovel);
		
		logger.info("Debitos a cobrar vigentes: " + debitos.size());
		
		assertTrue(debitos.size() == 1);
	}
	
	@Test
	@UsingDataSet("debito_cobrar_atualizacao_contas.yml")
	@ShouldMatchDataSet("debito_cobrar_atualizacao_contas_expected.yml")
	public void atualizarParaImoveisDeContasSemRotaAlternativa() throws Exception{
		List<Integer> imoveis = contaRepositorio.imoveisDeContasSemRotaAlternativa(1, 201404, 3, 1);
		
		repositorio.reduzirParcelasCobradas(201404, 1, imoveis);
	}
	
	@Test
	@UsingDataSet("debito_cobrar_atualizacao_contas.yml")
	@ShouldMatchDataSet("debito_cobrar_atualizacao_contas_alternativas_expected.yml")
	public void atualizarParaImoveisDeContasComRotaAlternativa() throws Exception{
		List<Integer> imoveis = contaRepositorio.imoveisDeContasComRotaAlternativa(2, 201404, 3, 1);
		
		repositorio.reduzirParcelasCobradas(201404, 1, imoveis);
		
	}
}