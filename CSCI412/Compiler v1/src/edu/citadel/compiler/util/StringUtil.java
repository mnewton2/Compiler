package edu.citadel.compiler.util;


public class StringUtil
  {
    /**
     * Formats an integer as right-justified within the specified field
     * width by prepending a sufficient number of blank spaces.
     */
    public static String format(int n, int fieldWidth)
      {
        String intStr = Integer.toString(n);

        if (intStr.length() >= fieldWidth)
            return intStr;
        else
          {
            StringBuffer buffer = new StringBuffer(fieldWidth);

            for (int i = intStr.length();  i < fieldWidth;  ++i)
                buffer.append(' ');

            buffer.append(intStr);

            return buffer.toString();
          }
      }
  }
