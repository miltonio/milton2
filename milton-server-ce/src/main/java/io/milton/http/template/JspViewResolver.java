/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.template;

import io.milton.common.View;
import io.milton.http.HttpManager;
import io.milton.servlet.OutputStreamWrappingHttpServletResponse;
import io.milton.servlet.ServletRequest;
import io.milton.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author brad
 */
public class JspViewResolver implements ViewResolver {
	private String basePath = "/templates/";
	private String theme = "bootstrap";
	private final ServletContext servletContext;

	public JspViewResolver(ServletContext servletContext) {
		if( servletContext == null ) {
			throw new RuntimeException("Must have a servlet context");
		}
		this.servletContext = servletContext;
	}
	
	
	@Override
	public TemplateProcessor resolveView(View view) {
		String jspPath = basePath + view.getTemplateName() + ".jsp";
		RequestDispatcher rd = servletContext.getRequestDispatcher(jspPath);
		if( rd == null ) {
			throw new RuntimeException("Template not found: jspPath=" + jspPath + " view template=" + view.getTemplateName());
		}
		return new JspTemplateProcessor(jspPath, rd);
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}
		
	public class JspTemplateProcessor implements TemplateProcessor {
		private final String jspPath;
		private final RequestDispatcher rd;

		public JspTemplateProcessor(String jspPath, RequestDispatcher rd) {
			this.jspPath = jspPath;
			this.rd = rd;
		}
						
		@Override
		public void execute(Map<String, Object> model, OutputStream out) {
			try {
				HttpServletRequest req = ServletRequest.getRequest();
				HttpServletResponse resp = new OutputStreamWrappingHttpServletResponse(ServletResponse.getResponse(), out);
				if( !model.containsKey("theme")) {
					model.put("theme", theme);
				}
				String path = HttpManager.request().getAbsolutePath();
				req.setAttribute("pagePath", path);
				req.setAttribute("model", model);
				rd.include(req, resp);
				resp.flushBuffer();
			} catch (ServletException e) {
				throw new RuntimeException(jspPath, e);
			} catch (IOException e) {
				throw new RuntimeException(jspPath, e);
			}
		}		
	}
}
