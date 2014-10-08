package br.gov.model.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import br.gov.model.faturamento.DebitoCreditoSituacao;

@Converter
public class DebitoCreditoSituacaoConverter implements AttributeConverter<DebitoCreditoSituacao, Short>{

	public Short convertToDatabaseColumn(DebitoCreditoSituacao attribute) {
		return attribute.getId();
	}

	public DebitoCreditoSituacao convertToEntityAttribute(Short dbData) {
        DebitoCreditoSituacao DebitoCreditoSituacao = null;
        for (DebitoCreditoSituacao item : DebitoCreditoSituacao.values()) {
            if (item.getId() == dbData.shortValue()) {
                DebitoCreditoSituacao = item;
                break;
            }
        }
        return DebitoCreditoSituacao;
	}
}
