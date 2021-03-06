package br.gov.model.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.time.DateUtils;
import static br.gov.model.util.Utilitarios.formataData;

import br.gov.model.operacao.Mes;

public class DateUtil {
	public List<Mes> mesesPeriodo(Date dataInicial, Date dataFinal) {
		List<Mes> meses = new LinkedList<Mes>();
		
		Locale locale = new Locale("pt", "BR");
		DateFormat format = new SimpleDateFormat("MMMMM", locale);
		DateFormat formatCurto = new SimpleDateFormat("MMM", locale);
		DateFormat mesAno = new SimpleDateFormat("MM/yyyy", locale);
		
		Date inicio = DateUtils.truncate(dataInicial, Calendar.DAY_OF_MONTH);
		Date fim = DateUtils.truncate(dataFinal, Calendar.DAY_OF_MONTH);
		int posicao = 1;
		while (!inicio.after(fim)) {
			Mes mes = new Mes();
			mes.setNumeral(DateUtils.toCalendar(inicio).get(Calendar.MONTH) + 1);
			mes.setPosicao(posicao++);
			mes.setNome(format.format(inicio));
			mes.setNomeCurto(formatCurto.format(inicio).toUpperCase());
			mes.setMesAno(mesAno.format(inicio));
			mes.setReferencia(Integer.valueOf(formataData(inicio, FormatoData.ANO_MES)));
			meses.add(mes);
			inicio = DateUtils.addMonths(inicio, 1);
		}
		return meses;
	}
	
	public static Date primeiroDiaMes(String data){
		String ano = data.substring(3, 7);
		String mes = data.substring(0, 2);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, Integer.valueOf(mes) - 1);
		cal.set(Calendar.YEAR, Integer.valueOf(ano));
		cal.set(Calendar.DAY_OF_MONTH, 1);
		
		return cal.getTime();
	}
	
	public static Date ultimoDiaMes(String data){
		String ano = data.substring(3, 7);
		String mes = data.substring(0, 2);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, Integer.valueOf(mes) - 1);
		cal.set(Calendar.YEAR, Integer.valueOf(ano));
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		
		return cal.getTime();
	}
}
