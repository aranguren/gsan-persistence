package br.gov.servicos.micromedicao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.persistence.util.SingleDeployment;
import br.gov.servicos.to.HidrometroTO;

@RunWith(Arquillian.class)
public class HidrometroInstalacaoHistoricoRepositorioTest extends SingleDeployment{

	@Inject
	private HidrometroInstalacaoHistoricoRepositorio repositorio;
	
	@Test
	@UsingDataSet({"hidrometroInstalacaoHistorico.yml"})
	public void instalacaoHidrometroPoco(){
		List<HidrometroTO> lista = repositorio.dadosInstalacaoHidrometro(1);
		
		HidrometroTO to = lista.get(0);
		
		assertEquals("123456", to.getNumero());
		assertEquals(33, to.getNumeroDigitosLeitura().intValue());
		assertEquals(3, to.getRateioTipo().intValue());
		assertEquals(1, to.getIdImovel().intValue());
		assertEquals(2, to.getMedicaoTipo().intValue());
	}
	
	@Test
	@UsingDataSet({"hidrometroInstalacaoHistorico.yml"})
	public void instalacaoHidrometroRede(){
		List<HidrometroTO> lista = repositorio.dadosInstalacaoHidrometro(2);
		
		HidrometroTO to = lista.get(0);
		
		assertEquals("654321", to.getNumero());
		assertEquals(9, to.getNumeroDigitosLeitura().intValue());
		assertEquals(3, to.getRateioTipo().intValue());
		assertEquals(2, to.getIdImovel().intValue());
		assertEquals(1, to.getMedicaoTipo().intValue());
	}
}