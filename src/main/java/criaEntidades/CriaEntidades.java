package criaEntidades;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CriaEntidades {

	private String arquivo;
	private String nomeJson;
	private LinkedHashMap<String, Object> objetos = new LinkedHashMap<>();
	private String entidades = "";
	
	public CriaEntidades(String arquivo, String nomeJson) {
		this.arquivo = arquivo;
		this.nomeJson = nomeJson;
	}
	
	private String lerArquivo() throws IOException {
		String json = String.join(" ", Files.readAllLines(Paths.get(arquivo), StandardCharsets.UTF_8));
		return json;
	}
	
	private LinkedHashMap<String, Object> getMap() throws IOException{
		String json = lerArquivo();
		LinkedHashMap<String, Object> map = new ObjectMapper().readValue(json, LinkedHashMap.class);
		return map;
	}
	
	private String criaCabecalho(String entidade) {
		String classe = StringUtils.capitalize(underscoreToCamelCase(entidade));
		String conteudoClasse = "Entidade: " + classe + ".java";
		conteudoClasse += "\n-----------------------------------------------";
		conteudoClasse += "\npublic class " + classe + "{";
		conteudoClasse += "\n    LinkedHashMap<String, Object> " + underscoreToCamelCase(entidade) + " = new LinkedHashMap<>();";
		conteudoClasse += "\n    public " + classe + "(";
		return conteudoClasse;
	}
	
	private String criaSetGet(String entidade) {
		String metodos = "";
		String classe = StringUtils.capitalize(underscoreToCamelCase(entidade));
		metodos += "\n    public LinkedHashMap<String, Object> get"+classe+"() {";
		metodos += "\n        return "+underscoreToCamelCase(entidade)+";";
		metodos += "\n    }\n}";
		
		return metodos;
	}
	
	private Map<String, String> getByTipo(Map.Entry<String, Object> entry, String entidade){
		String tipo = entry.getValue().getClass().toString();
		String objeto = underscoreToCamelCase(entry.getKey());
		String tituloMetodo = "";
		String conteudo = "";
		if (tipo.contains("ArrayList")) {
			List<Object> objetoLista = (List<Object>)entry.getValue();
			String tipoLista = objetoLista.get(0).getClass().toString();
			if (tipoLista.contains("Map")) {
				tituloMetodo += "List<LinkedHashMap<String, Object>> list"+StringUtils.capitalize(objeto);
				conteudo += "\n        "+ entidade.toLowerCase() + ".put(\""+ entry.getKey() + "\",list"
						+ StringUtils.capitalize(objeto) + ");";
				objetos.put(entry.getKey(), entry.getValue());
			}else {
				tituloMetodo += "List<Object> list"+StringUtils.capitalize(objeto);
				conteudo += "\n        "+ entidade.toLowerCase() + ".put(\""+ entry.getKey() + "\",list"
						+ StringUtils.capitalize(objeto) + ");";
				objetos.put(entry.getKey(), entry.getValue());
			}
			
			
		}else if (tipo.contains("Map")) {
			tituloMetodo += "LinkedHashMap<String, Object> "+ objeto;
			conteudo += "\n        "+ entidade.toLowerCase() + ".put(\"" + entry.getKey() + "\"," + objeto + ");";
			objetos.put(entry.getKey(), entry.getValue());
		}else {
			tituloMetodo += "Object " + objeto;
			conteudo += "\n        " + entidade.toLowerCase() + ".put(\""+ entry.getKey() + "\"," + objeto + ");";
		}
		Map<String, String> map = new LinkedHashMap<>();
		map.put("tituloMetodo", tituloMetodo);
		map.put("conteudo", conteudo);
		return map;
	}
	
	private String criaConteudoConstrutor(String entidade, LinkedHashMap<String, Object> subMap) {
		String tituloMetodo = criaCabecalho(entidade);
		String conteudo = "";
		int count = 0;
		for (Map.Entry<String, Object> entry : subMap.entrySet()) {
			if (count == 4) {
				tituloMetodo += "\n                     ";
				count = 0;
			}
			Map<String, String> mapDados = getByTipo(entry, entidade);
			tituloMetodo += mapDados.get("tituloMetodo");
			conteudo += mapDados.get("conteudo");
			count++;
			tituloMetodo += ", ";
		}
		tituloMetodo = tituloMetodo.substring(0, tituloMetodo.length() - 2) + "){";
		return tituloMetodo + conteudo + "\n    }";

	}
		
	private String underscoreToCamelCase(String valor) {
		List<String> listValor = Arrays.asList(valor.toLowerCase().split("_"));
		String valorFinal = listValor.get(0).toLowerCase();
		if (listValor.size() > 1) {
			for (int i = 1; i < listValor.size(); i++) {
				valorFinal += StringUtils.capitalize(listValor.get(i).toLowerCase());
			}
			
		}
		return valorFinal;
	}
	
	private void criaClasse() {
		LinkedHashMap<String, Object> subMap = new LinkedHashMap<>();
		
		Object value = objetos.entrySet().iterator().next().getValue();
		String key = objetos.entrySet().iterator().next().getKey();
		
		String tipo = value.getClass().toString();
		criaInstancia(key.toString(), value);
		if (tipo.contains("Map")) {
			subMap = (LinkedHashMap<String, Object>) value;
			
			String conteudo = criaConteudoConstrutor(key.toString(), subMap);
			String metodos = criaSetGet(key.toString());
			System.out.println(conteudo);
			System.out.println(metodos);
			
		}else if(tipo.contains("ArrayList")) {
			List<Object> listObjeto = (List<Object>) value;
			if(listObjeto.get(0).getClass().toString().contains("Map")) {
				List<LinkedHashMap<String, Object>> listMap = (List<LinkedHashMap<String,Object>>) value;
				subMap = listMap.get(0);
				
				String conteudo = criaConteudoConstrutor(key.toString(), subMap);
				String metodos = criaSetGet(key.toString());
				System.out.println(conteudo);
				System.out.println(metodos);
			}
			
		}
		
		
	}
	
	private String getValores(LinkedHashMap<String, Object> map) {
		int count = 0;
		String valores = "";
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (count == 8) {
				valores += "\n                     ";
				count = 0;
			}
			String _tipo = entry.getValue().getClass().toString();
			if (_tipo.contains("Map")) {
				valores += underscoreToCamelCase(entry.getKey())+".get"+StringUtils.capitalize(underscoreToCamelCase(entry.getKey()))+"()";
			}else if (_tipo.contains("list")) {
				valores += "list"+StringUtils.capitalize(underscoreToCamelCase(entry.getKey()));
			}else if (_tipo.contains("String")) {
				valores += "\""+entry.getValue()+"\"";
			}else {
				valores += entry.getValue();
			}
			
			count++;
			valores += ", ";
						
		}
		return valores.substring(0, valores.length() - 2);
	}
	
	private String criaInstancia(String entidade, Object object) {
		String tipo = object.getClass().toString();
		String classe = StringUtils.capitalize(underscoreToCamelCase(entidade));
		String objeto = underscoreToCamelCase(entidade);
		LinkedHashMap<String, Object> map;
		
		String valores = "";
		int count = 0;
		
		if (tipo.contains("Map")) {
			map = (LinkedHashMap<String, Object>) object;
			valores = getValores(map);
			entidades = "\n"+classe+" "+objeto+" = new "+classe+"("+valores+");"+entidades;
		}else if (tipo.contains("List")) {
			List<Object> listObjeto = (List<Object>) object;
			if(listObjeto.get(0).getClass().toString().contains("Map")) {
				List<LinkedHashMap<String, Object>> listMap = (List<LinkedHashMap<String,Object>>) object;
				for (LinkedHashMap<String, Object> _map : listMap) {
					valores += getValores(_map);
				}
				entidades = "\nlist"+classe+".add("+objeto+".get"+classe+"())"+entidades;
				entidades = "\nList<LinkedHashMap<String, Object>> list"+classe+" = new ArrayList<LinkedHashMap<String, Object>>();"+entidades;
				entidades = "\n"+classe+" "+objeto+" = new "+classe+"("+valores+");"+entidades;
			}
			
			
		}
		entidades = "\n"+entidades;
		return entidades;
	}
	
	private void exibeMap(LinkedHashMap<String, Object> map, String entidade) {
		String conteudo = criaConteudoConstrutor(entidade, map);
		System.out.println(conteudo);
		String metodos = criaSetGet(entidade);
		System.out.println(metodos);
		System.out.println("##--------------------------------------");
		criaInstancia(entidade, map);
		while (!objetos.isEmpty()) {
			criaClasse();
			String key = objetos.entrySet().iterator().next().getKey();
			objetos.remove(key);
		}
		System.out.println("----------------------------------------");
		
		System.out.println(entidades);
	}
	
	public void exibeMaps() throws IOException {
		exibeMap(getMap(), this.nomeJson);
	}
}
