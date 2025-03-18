package no.nav.dokarkivavlevering.avlevering.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@ConfigurationPropertiesBinding
public class LocalDateConverter implements Converter<String, LocalDate> {
	@Override
	public LocalDate convert(String source) {
		if (source == null) {
			return null;
		}
		return LocalDate.parse(source);
	}
}