package com.gitplex.server.web.util;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.util.string.Strings;

import com.gitplex.server.web.util.mapper.PathAwareUrl;
import com.google.common.collect.Lists;

public class AbsoluteUrlRenderer extends UrlRenderer {

	public AbsoluteUrlRenderer(Request request) {
		super(request);
	}

	@Override
	public String renderUrl(Url url) {
		String renderedUrl;
		if (shouldRenderAsFull(url)) {
			if (!(url.isFull() || url.isContextAbsolute())) {
				String relativeUrl = renderRelativeUrl(url);
				Url relative = Url.parse(relativeUrl, url.getCharset());
				relative.setHost(url.getHost());
				relative.setPort(url.getPort());
				relative.setProtocol(url.getProtocol());
				renderedUrl = renderFullUrl(relative);
			} else {
				renderedUrl = renderFullUrl(url);
			}
		} else {
			renderedUrl = renderAbsoluteUrl(url);
		}
		return renderedUrl;
	}

	private String renderAbsoluteUrl(Url url) {
		if (url.isFull()) {
			return url.toString();
		} else if (url.getSegments().isEmpty()) {
			Url absolute = copy(url);
			absolute.prependLeadingSegments(Lists.newArrayList("", ""));
			return absolute.toString();
		} else if (Strings.isEmpty(url.getSegments().get(0))) {
			return url.toString();
		} else {
			Url absolute = copy(url);
			absolute.prependLeadingSegments(Lists.newArrayList(""));
			return absolute.toString();
		}
	}

	private Url copy(Url url) {
		if (url instanceof PathAwareUrl)
			return new PathAwareUrl(url);
		else
			return new Url(url);
	}
}