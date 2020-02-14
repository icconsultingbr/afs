package com.anoto.patterncore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class LicenseManager
{
  private static char[] keys = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

  private static String numToKey(long number, long scramble)
  {
    StringBuffer result = new StringBuffer();
    int thisChar = 0;

    while (number > 0L)
    {
      thisChar = (int)((number + scramble) % keys.length);
      number /= keys.length;
      result.append(keys[thisChar]);
    }
    if (result.length() == 0)
      return "" + keys[thisChar];

    return new String(result);
  }

  private static long keyToNum(String key, long unscramble)
  {
    long result = 0L;
    int foundAt = 0;

    for (int i = key.length() - 1; i >= 0; --i)
    {
      for (int j = 0; j < keys.length; ++j)
      {
        if (keys[j] == key.charAt(i))
          foundAt = j;
      }

      long add = (foundAt - unscramble) % keys.length;
      if (add < 0L)
      {
        add += keys.length;
      }
      result = keys.length * result + add;
    }
    return result;
  }

  public static String pageAddressToLicense(PageAddress address)
  {
    long atom = address.longValue();

    StringBuffer result = new StringBuffer();

    long checksum = (atom % 77L + 32212774L) % 77L;

    result.append(numToKey(address.getSegment() + 10000000L, checksum + 4L));
    result.append("-");
    result.append(numToKey(address.getShelf() + 10000000L, checksum + 8L));
    result.append("-");
    result.append(numToKey(address.getBook() + 10000000L, checksum + 16L));
    result.append("-");
    result.append(numToKey(address.getPage() + 10000000L, checksum + 32L));
    result.append("-");
    result.append(numToKey(checksum, 0L));

    return new String(result);
  }

  @SuppressWarnings("deprecation")
public static PageAddress licenseToPageAddress(String license)
    throws InvalidLicenseException
  {
    int segment;
    int shelf;
    int book;
    int page;
    long checksum;
    try
    {
      StringTokenizer tokenizer = new StringTokenizer(license, "-");

      String segmentStr = tokenizer.nextToken();
      String shelfStr = tokenizer.nextToken();
      String bookStr = tokenizer.nextToken();
      String pageStr = tokenizer.nextToken();
      String checksumStr = tokenizer.nextToken();

      checksum = keyToNum(checksumStr, 0L);
      segment = (int)(keyToNum(segmentStr, checksum + 4L) - 10000000L);
      shelf = (int)(keyToNum(shelfStr, checksum + 8L) - 10000000L);
      book = (int)(keyToNum(bookStr, checksum + 16L) - 10000000L);
      page = (int)(keyToNum(pageStr, checksum + 32L) - 10000000L);
    }
    catch (Exception e)
    {
      throw new InvalidLicenseException();
    }

    PageAddress result = new PageAddress(segment, shelf, book, page);

    long atom = result.longValue();

    if (checksum != (atom % 77L + 32212774L) % 77L)
    {
      throw new InvalidLicenseException();
    }

    return result;
  }
  
  @SuppressWarnings("deprecation")
private static void doReadWritePadFile(String address, String formType, String padFile) {

      try {
    	  File xml = new File("pads//" + formType + "//" + padFile);
    	  
    	  //if (xml.canRead()) {
    		  // Parse do xml.
              DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
              fabrica.setValidating(false); //Ignore DTD associada.
              fabrica.setNamespaceAware(false); //Ignore NameSpace associada.
              DocumentBuilder construtor = fabrica.newDocumentBuilder();
              Document documento;
				
              documento = construtor.parse(new FileInputStream(xml));
              
              NodeList nodeListPaperLicense = documento.getElementsByTagName("paperlicense");
              Element nodePaperLicense = (Element) (nodeListPaperLicense.item(0));
              
              String[] split = address.split("\\.");
              int segment = Integer.parseInt(split[0]);
              int shelf = Integer.parseInt(split[1]);
              PageAddress addr = new PageAddress(segment, shelf, -1, -1);
              String key = pageAddressToLicense(addr);
              
              nodePaperLicense.setAttribute("address", address);
              nodePaperLicense.setAttribute("key", key);
              
              NodeList nodeListPage = documento.getElementsByTagName("page");
              
              Element nodePageTwo = (Element) (nodeListPage.item(1));
              nodePageTwo.setAttribute("address", address);
              
              Element nodePageOne = (Element) (nodeListPage.item(0));
              nodePageOne.setAttribute("address", address.replaceAll("\\*", "0"));
              
              DOMSource source = new DOMSource(documento);
      		  StringWriter sw0 = new StringWriter();
              StreamResult sw1 = new StreamResult(sw0);
               
              TransformerFactory tFactory = TransformerFactory.newInstance();
              Transformer transformer = tFactory.newTransformer();
              transformer.setOutputProperty("encoding", "UTF-8");
              transformer.setOutputProperty("standalone", "no");
              transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "pad1.0");
              transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://dtd.anoto.com/dtds/pad1.0.dtd");

              transformer.transform(source, sw1);
              
              FileOutputStream fos = new FileOutputStream("pads//" + formType + "//" + segment + "." + shelf + ".X.X.pad");
              fos.write(sw0.toString().getBytes());
              fos.flush();
              
              sw0.close();
    	  //}

      } catch (Exception e) {
          e.printStackTrace();
      }

  }
  
  public static void main(String[] args) {
	  // Definir o Form Type
	  //String formType = "FA2";
	  String formType = "FICHA_TORNOZELEIRA2015";
	  //String formType = "LIVESCRIBE";
	  // Definir o Pad File
	  String padFile = "81.2501.X.X.pad";
	  //String padFile = "81.2258.X.X.pad";
	  //String padFile = "2208.0.X.X.pad";
	  
	  FilenameFilter filter = new FilenameFilter() {
          public boolean accept(File b, String name) { 
              return name.endsWith(".pad"); 
          } 
      };
      
      File dirPads = new File("pads//" + formType);
      String[] pads = dirPads.list(filter);
      String lastPad = pads[pads.length - 1];
      
      String[] pad = lastPad.split("\\.");
      int segment = Integer.parseInt(pad[0]);
      int shelf = Integer.parseInt(pad[1]);
	  
	  int i = 1;
	  
	  while (i <= 10) {
		  doReadWritePadFile(segment + "." + (shelf + i) + ".*.*", formType, padFile);
		  i ++;
	  }
  }
}