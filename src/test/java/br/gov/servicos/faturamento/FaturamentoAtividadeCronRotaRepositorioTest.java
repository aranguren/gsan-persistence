package br.gov.servicos.faturamento;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.CleanupStrategy;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.servicos.test.ShrinkWrapBuilder;
import br.gov.servicos.to.CronogramaFaturamentoRotaTO;

@RunWith(Arquillian.class)
public class FaturamentoAtividadeCronRotaRepositorioTest {

	@Deployment
    public static Archive<?> createDeployment() {
		return ShrinkWrapBuilder.createDeployment();
    }
	
	@Inject
	FaturamentoAtividadeCronRotaRepositorio repositorio;
	
	@Test
	@UsingDataSet("faturamentoAtividadeCronRota.yml")
	@Cleanup(phase = TestExecutionPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
	public void faturamentoAtividadeCronRotaNulo() throws Exception {
		CronogramaFaturamentoRotaTO result = repositorio.pesquisaFaturamentoAtividadeCronogramaRota(1, 1, 201404);
		
		assertEquals(null, result);
	}
	
	@Test
	@UsingDataSet("faturamentoAtividadeCronRota.yml")
	@Cleanup(phase = TestExecutionPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
	public void faturamentoAtividadeCronRota() throws Exception {
		CronogramaFaturamentoRotaTO result = repositorio.pesquisaFaturamentoAtividadeCronogramaRota(1, 1, 201405);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		assertEquals("2014-06-01", format.format(result.getDataVencimentoConta()));
	}
	
}
