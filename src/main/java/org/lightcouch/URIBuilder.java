/*
 * Copyright (C) 2011 lightcouch.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lightcouch;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for construction of HTTP request URIs.
 * @since 0.0.2
 * @author Ahmed Yehia
 * 
 */
class URIBuilder {
	private String scheme;
	private String host;
	private int port;
	private String path = "";
	/* The final query */
	private final StringBuilder query = new StringBuilder();
	/* key=value params */
	private final List<NameValuePair> qParams = new ArrayList<NameValuePair>();
	private boolean uriEncoded = false;

	public static URIBuilder buildUri() {
		return new URIBuilder();
	}

	public static URIBuilder buildUri(URI uri) {
		URIBuilder builder = URIBuilder.buildUri().scheme(uri.getScheme())
				.host(uri.getHost()).port(uri.getPort()).path(uri.getPath());
		return builder;
	}

	public URI build() {
		if(uriEncoded) {
			prepareQuery();
			return createUriEncoded();
		} else
			return createUri();
	}

	public URIBuilder scheme(String scheme) {
		this.scheme = scheme;
		return this;
	}

	public URIBuilder host(String host) {
		this.host = host;
		return this;
	}

	public URIBuilder port(int port) {
		this.port = port;
		return this;
	}

	public URIBuilder path(String path) {
		this.path += path;
		return this;
	}
	
	public URIBuilder path(String path, boolean encode) {
		if (encode) {
			this.uriEncoded = true;
			try {
				this.path += URLEncoder.encode(path, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			path(path);
		}
		return this;
	}

	public URIBuilder query(String name, Object value) {
		if (name != null && value != null) {
			this.qParams.add(new BasicNameValuePair(name, String.valueOf(value)));
		}
		return this;
	}

	public URIBuilder query(String query) {
		if (query != null)
			this.query.append(query);
		return this;
	}

	public URIBuilder query(Params params) {
		if (params.getParams() != null)
			this.qParams.addAll(params.getParams());
		return this;
	}
	
	// private
	
	private URI createUri() {
		try {
			org.apache.http.client.utils.URIBuilder uriBuilder = new org.apache.http.client.utils.URIBuilder();
			uriBuilder.setScheme(scheme);
			uriBuilder.setHost(host);
			uriBuilder.setPort(port);
			uriBuilder.setPath(path);
			if (query.length() > 0) {
				uriBuilder.setCustomQuery(query.toString());
			} else if (!qParams.isEmpty()) {
				uriBuilder.setParameters(qParams);
			}
			return uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private URI createUriEncoded() {
		try {
			String q = (query.length() == 0) ? "" : "?" + query;
			String uri = String.format("%s://%s:%s%s%s", new Object[]{scheme, host, port, path, q});
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void prepareQuery() {
		for (int i = 0; i < qParams.size(); i++) {
			String amp = (i != qParams.size() - 1) ? "&" : "";
			query.append(qParams.get(i) + amp);
		}
	}
	
}
