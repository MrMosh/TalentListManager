package utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.renjin.invoke.codegen.VarArgApplyBuilder;

public class SoFifaParser {

	public static Map<String, String> setMetaData(Document doc, Map<String, String> map) {
		Element meta = doc.selectFirst(".meta");
		List<TextNode> first = meta.childNodes().stream()
				.filter(node -> node instanceof TextNode && !((TextNode) node).getWholeText().trim().isEmpty())
				.map(arg -> (TextNode) arg).collect(Collectors.toList());
		map.put("Name", first.get(0).text().trim());
		processAdditionalData(first.get(1), map);
		Element countryElement = meta.selectFirst("a[title]");
		map.put("Country", countryElement.attr("title"));
		setPositions(map, meta);
		return map;
	}

	private static void setPositions(Map<String, String> map, Element meta) {
		Elements positions = meta.select("span");
		for (Element span : positions) {
			if (span.attr("class") != null && span.attr("class").trim().toLowerCase().contains("pos")) {
				if (map.containsKey("Positions"))
					map.put("Positions", map.get("Positions").concat(" " + span.html()));
				else
					map.put("Positions", span.html());
			}
		}
	}

	private static void processAdditionalData(TextNode textNode, Map<String, String> map) {
		String text = textNode.text().trim();
		List<String> splittedAttributes = Arrays.asList(text.split("[\\(||\\)]"));
		for (String attribute : splittedAttributes) {
			if (attribute.contains("Age"))
				map.put("Age", attribute.replaceFirst("Age", "").trim());
			if (attribute.contains("cm") && attribute.contains("kg")) {
				List<String> heightNWeight = Arrays.asList(attribute.trim().split("\\s+"));
				for (String physical : heightNWeight) {
					if (physical.contains("cm"))
						map.put("Height", physical.replaceFirst("cm", "").trim());
					if (physical.contains("kg"))
						map.put("Weight", physical.replaceFirst("kg", "").trim());
				}

			}

		}

	}

	public static Map<String, String> setEntry(Document doc, String selector, Map<String, String> map) {
		if (selector.equals("Loaned From"))
			selector.length();

		Element element = doc.selectFirst("li:contains(" + selector + ")");
		if (element == null)
			element = doc.selectFirst("div:containsOwn(" + selector + ")");
		if (element != null) {
			Element span = element.selectFirst("span");
			if (span != null) {
				map.put(selector, span.html());
			} else {
				Optional<TextNode> first = element.childNodes().stream()
						.filter(node -> node instanceof TextNode && !((TextNode) node).getWholeText().isEmpty())
						.map(arg -> (TextNode) arg).findFirst();
				if (first.isPresent())
					map.put(selector, first.get().getWholeText());
				else {
					Element joinedFrom = element.selectFirst("a");
					if (element != null)
						map.put(selector, joinedFrom.html());
				}
			}
		}
		if (!map.containsKey(selector))
			map.put(selector, "");
		return map;
	}

	public static Map<String, String> setCombinedAttributes(Document doc, Map<String, String> map, String id) {
		Elements scriptTags = doc.getElementsByTag("script");
		for (Element tag : scriptTags)
			for (DataNode node : tag.dataNodes())
				if (node.getWholeData().contains(id)) {
					List<String> vars = Arrays.asList(node.getWholeData().split(";", -1));
					putPlayerValues(map, vars);
				}
		return map;
	}

	private static void putPlayerValues(Map<String, String> map, List<String> vars) {
		for (String var : vars) {
			if (var.contains("pointPAC"))
				map.put("firstAttribute", var.replaceAll("[^0-9]", ""));

			if (var.contains("pointPAS"))
				map.put("secondAttribute", var.replaceAll("[^0-9]", ""));

			if (var.contains("pointSHO"))
				map.put("thirdAttribute", var.replaceAll("[^0-9]", ""));

			if (var.contains("pointDRI"))
				map.put("fourthAttribute", var.replaceAll("[^0-9]", ""));

			if (var.contains("pointDEF"))
				map.put("fifthAttribute", var.replaceAll("[^0-9]", ""));

			if (var.contains("pointPHY"))
				map.put("sixthAttribute", var.replaceAll("[^0-9]", ""));
		}
	}

}
