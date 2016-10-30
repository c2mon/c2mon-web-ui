package cern.c2mon.web.ui.config;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * {@link Filter} implementation that forwards non-API requests to the index
 * page for the frontend to handle.
 *
 * @author Justin Lewis Salmon
 */
@Component
public class HttpFilter extends OncePerRequestFilter {

  private String[] staticResourceExtensions = new String[] {
      "html", "js", "map", "css", "ttf", "woff", "woff2"
  };

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    if (request.getRequestURI().startsWith("/api") || isStaticResource(request.getRequestURI())) {
      filterChain.doFilter(request, response);
    } else {
      // Forward to home page so that route is preserved.
      request.getRequestDispatcher("/").forward(request, response);
    }
  }

  private boolean isStaticResource(String uri) {
    return FilenameUtils.isExtension(uri, staticResourceExtensions);
  }
}
