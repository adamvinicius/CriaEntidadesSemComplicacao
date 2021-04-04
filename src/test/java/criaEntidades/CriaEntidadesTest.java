package criaEntidades;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class CriaEntidadesTest {

	@Test
	void testCriaEntidades() throws IOException {
		CriaEntidades criaEntidades = new CriaEntidades("lib/teste.json", "Token");
		criaEntidades.exibeMaps();
	}

}
