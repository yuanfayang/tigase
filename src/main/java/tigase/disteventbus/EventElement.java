package tigase.disteventbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tigase.xml.Element;

public class EventElement extends Element {

	public EventElement(Element element) {
		super(element);
	}

	public EventElement(String elemName, String xmlns) {
		super(elemName);
		setXMLNS(xmlns);
	}

	public void addElementsCollection(String elemName, Collection<Element> values) {
		Element e = new Element(elemName);
		if (values != null)
			for (Element i : values) {
				e.addChild(i);
			}
		addChild(e);
	}

	public void addString(String elemName, String value) {
		addChild(new Element(elemName, value));
	}

	public void addStringsArray(String elemName, String[] values) {
		Element e = new Element(elemName);
		if (values != null)
			for (String string : values) {
				e.addChild(new Element("value", string));
			}
		addChild(e);
	}

	public void addStringsCollection(String elemName, Collection<String> values) {
		Element e = new Element(elemName);
		if (values != null)
			for (String string : values) {
				e.addChild(new Element("value", string));
			}
		addChild(e);
	}

	public String[] getElementsArray(String elemName) {
		Collection<String> cc = getStringsCollection(elemName);
		if (cc == null)
			return null;
		return cc.toArray(new String[] {});
	}

	public Collection<Element> getElementsCollection(String elemName) {
		Element c = getChild(elemName);
		if (c == null)
			return null;
		return new ArrayList<Element>(c.getChildren());
	}

	public String getString(String elemName) {
		Element e = new Element(elemName);
		if (e == null)
			return null;
		return e.getCData();
	}

	public List<String> getStringsCollection(String elemName) {
		Collection<Element> r = getElementsCollection(elemName);
		if (r == null)
			return null;
		ArrayList<String> result = new ArrayList<String>();

		for (Element e : r) {
			result.add(e.getCData());
		}

		return result;
	}

}
