package moe.knox.prototype;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

public class CustomHtmlWhitelist extends Whitelist {
	@Override
	protected boolean isSafeTag(String tag) {
		return true;
	}

	@Override
	protected boolean isSafeAttribute(String tagName, Element el, Attribute attr) {
		return true;
	}
}
