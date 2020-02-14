package com.anoto.javax.servlet.http;

import com.anoto.javax.servlet.ServletInputStreamImpl;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@SuppressWarnings("rawtypes")
public class ChunkedRequest
  implements HttpServletRequest
{
  public static final String KEY_CONTENT_LENGTH = "Content-Length";
  public static final String KEY_TRANSFER_ENCODING = "Transfer-Encoding";
  public static final String KEY_CONTENT_TYPE = "Content-Type";
  private HttpServletRequest request;
  private ServletInputStream servletInputStream;
  private String contentLengthString;
  private int contentLength;
  private byte[] contentBuffer;

  public ChunkedRequest(HttpServletRequest request)
    throws IOException
  {
    this(request, false);
  }

  public ChunkedRequest(HttpServletRequest request, boolean forceBuffering)
    throws IOException
  {
    this.request = request;
    ServletContext context = request.getSession().getServletContext();

    String transferEncoding = request.getHeader("Transfer-Encoding");
    if ((forceBuffering) || (-1 == request.getContentLength()) || 
      (transferEncoding != null))
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      InputStream in = request.getInputStream();
      byte[] buf = new byte[256];
      int count = in.read(buf);
      while (count > 0)
      {
        baos.write(buf, 0, count);
        count = in.read(buf);
      }
      baos.flush();
      in.close();

      this.contentBuffer = baos.toByteArray();
      ByteArrayInputStream bais = new ByteArrayInputStream(this.contentBuffer);
      baos.close();
      this.servletInputStream = new ServletInputStreamImpl(bais);
      this.contentLength = bais.available();
      this.contentLengthString = String.valueOf(this.contentLength);
      if (transferEncoding != null)
      {
        context.log("ChunkedRequest: Content-Length: " + 
          this.contentLengthString + '(' + request.getContentLength() + ')');
      }
    }
    else
    {
      this.contentBuffer = new byte[0];
      this.servletInputStream = request.getInputStream();
      this.contentLength = request.getContentLength();
      this.contentLengthString = request.getHeader("Content-Length");
    }
  }

  public byte[] getContentBuffer()
  {
    return this.contentBuffer;
  }

  public HttpSession getSession(boolean create)
  {
    return this.request.getSession(create);
  }

  public String getParameter(String name)
  {
    return this.request.getParameter(name);
  }

  public boolean isUserInRole(String role)
  {
    return this.request.isUserInRole(role);
  }

  public long getDateHeader(String name)
  {
    return this.request.getDateHeader(name);
  }

  public String getHeader(String name)
  {
    if ("Content-Length".equalsIgnoreCase(name))
    {
      return this.contentLengthString;
    }
    if ("Content-Type".equalsIgnoreCase(name))
    {
      String contentType = this.request.getHeader("Content-Type");
      if ((contentType != null) && 
        (contentType.equalsIgnoreCase("multipart/form-data")))
      {
        return "multipart/form-data; boundary=xxcxoxvxexlxuxsxxxrxoxuxtxexrx";
      }
      if ((contentType != null) && 
        (contentType.equalsIgnoreCase("application/octet-stream")))
      {
        return "application/octet-stream; encoding=PGC";
      }

      if ((contentType != null) && (-1 < contentType.indexOf("xxcxoxvxexlxuxsxxxrxoxuxtxexrx")))
      {
        int beginPos = contentType.indexOf("xxcxoxvxexlxuxsxxxrxoxuxtxexrx");
        if (beginPos < contentType.indexOf(44, beginPos))
        {
          return "multipart/form-data; boundary=xxcxoxvxexlxuxsxxxrxoxuxtxexrx";
        }
      }
    }
    return this.request.getHeader(name);
  }

  public Object getAttribute(String name)
  {
    return this.request.getAttribute(name);
  }

  public int getIntHeader(String name)
  {
    if ("Content-Length".equals(name))
    {
      return this.contentLength;
    }
    return this.request.getIntHeader(name);
  }

  public Enumeration getHeaders(String name)
  {
    return this.request.getHeaders(name);
  }

  public void setCharacterEncoding(String env)
    throws UnsupportedEncodingException
  {
    this.request.setCharacterEncoding(env);
  }

  public void removeAttribute(String name)
  {
    this.request.removeAttribute(name);
  }

  public RequestDispatcher getRequestDispatcher(String path)
  {
    return this.request.getRequestDispatcher(path); }

  /**
   * @deprecated
   */
  public String getRealPath(String path) {
    return null;
  }

  public String[] getParameterValues(String name)
  {
    return this.request.getParameterValues(name);
  }

  public void setAttribute(String str, Object obj)
  {
    this.request.setAttribute(str, obj);
  }

  public String getRemoteAddr()
  {
    return this.request.getRemoteAddr();
  }

  public HttpSession getSession()
  {
    return this.request.getSession();
  }

  public StringBuffer getRequestURL()
  {
    return this.request.getRequestURL();
  }

  public String getRequestedSessionId()
  {
    return this.request.getRequestedSessionId();
  }

  public Cookie[] getCookies()
  {
    return this.request.getCookies();
  }

  public String getContentType()
  {
    return getHeader("Content-Type");
  }

  public String getLocalAddr()
  {
    return this.request.getLocalAddr();
  }

  public String getRequestURI()
  {
    return this.request.getRequestURI();
  }

  public String getPathInfo()
  {
    return this.request.getPathInfo();
  }

  public int getLocalPort()
  {
    return this.request.getLocalPort();
  }

  public String getAuthType()
  {
    return this.request.getAuthType();
  }

  public String getContextPath()
  {
    return this.request.getContextPath();
  }

  public int getContentLength()
  {
    return this.contentLength;
  }

  public String getCharacterEncoding()
  {
    return this.request.getCharacterEncoding();
  }

  public boolean isRequestedSessionIdFromURL()
  {
    return false;
  }

  public boolean isRequestedSessionIdValid()
  {
    return this.request.isRequestedSessionIdValid();
  }

  public String getMethod()
  {
    return this.request.getMethod();
  }

  public Enumeration getHeaderNames()
  {
    return this.request.getHeaderNames(); }

  /**
   * @deprecated
   */
  public boolean isRequestedSessionIdFromUrl() {
    return false;
  }

  public String getRemoteUser()
  {
    return this.request.getRemoteUser();
  }

  public String getLocalName()
  {
    return this.request.getLocalName();
  }

  public BufferedReader getReader()
    throws IOException
  {
    return this.request.getReader();
  }

  public boolean isSecure()
  {
    return this.request.isSecure();
  }

  public String getQueryString()
  {
    return this.request.getQueryString();
  }

  public String getScheme()
  {
    return this.request.getScheme();
  }

  public int getRemotePort()
  {
    return this.request.getRemotePort();
  }

  public Map getParameterMap()
  {
    return this.request.getParameterMap();
  }

  public Enumeration getLocales()
  {
    return this.request.getLocales();
  }

  public boolean isRequestedSessionIdFromCookie()
  {
    return this.request.isRequestedSessionIdFromCookie();
  }

  public Locale getLocale()
  {
    return this.request.getLocale();
  }

  public Enumeration getAttributeNames()
  {
    return this.request.getAttributeNames();
  }

  public ServletInputStream getInputStream()
    throws IOException
  {
    return this.servletInputStream;
  }

  public String getServletPath()
  {
    return this.request.getServletPath();
  }

  public Principal getUserPrincipal()
  {
    return this.request.getUserPrincipal();
  }

  public String getProtocol()
  {
    return this.request.getProtocol();
  }

  public String getPathTranslated()
  {
    return this.request.getPathTranslated();
  }

  public int getServerPort()
  {
    return this.request.getServerPort();
  }

  public String getRemoteHost()
  {
    return this.request.getRemoteHost();
  }

  public Enumeration getParameterNames()
  {
    return this.request.getParameterNames();
  }

  public String getServerName()
  {
    return this.request.getServerName();
  }
}