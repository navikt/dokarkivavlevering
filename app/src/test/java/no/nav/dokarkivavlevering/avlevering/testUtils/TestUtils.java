package no.nav.dokarkivavlevering.avlevering.testUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestUtils {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd k:mm:ss[.SSS][.SS]");

	public static LocalDateTime toLocalDateTime(String dato) {
		return LocalDateTime.parse(dato, formatter);
	}
}
