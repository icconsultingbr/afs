package com.anoto.patterncore;

import java.util.StringTokenizer;


public abstract class LicenseManagerOriginal
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
  public static void main(String[] args) {
	  PageAddress addr = new PageAddress(90, 0, -1, -1);
	  System.out.println(pageAddressToLicense(addr));
  }
}