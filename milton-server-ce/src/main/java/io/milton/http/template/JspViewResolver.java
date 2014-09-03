/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
