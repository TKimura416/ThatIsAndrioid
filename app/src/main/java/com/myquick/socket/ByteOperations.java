package com.myquick.socket;


class ByteOperations {
	
	  /**
	   * <p>
	   * Removes spaces (char &lt;= 32) from both start and ends of this bytes
	   * array, handling <code>null</code> by returning <code>null</code>.
	   * </p>
	   * Trim removes start and end characters &lt;= 32.
	   * 
	   * <pre>
	   *  StringUtils.trim(null)          = null
	   *  StringUtils.trim(&quot;&quot;)            = &quot;&quot;
	   *  StringUtils.trim(&quot;     &quot;)       = &quot;&quot;
	   *  StringUtils.trim(&quot;abc&quot;)         = &quot;abc&quot;
	   *  StringUtils.trim(&quot;    abc    &quot;) = &quot;abc&quot;
	   * </pre>
	   * 
	   * @param bytes the byte array to be trimmed, may be null
	   * 
	   * @return the trimmed byte array
	   */
	  public static final byte[] trim( byte[] bytes )
	  {
	      if ( isEmpty( bytes ) )
	      {
	          return new byte[0];
	      }

	      int start = trimLeft( bytes, 0 );
	      int end = trimRight( bytes, bytes.length - 1 );

	      int length = end - start + 1;

	      if ( length != 0 )
	      {
	          byte[] newBytes = new byte[end - start + 1];

	          System.arraycopy( bytes, start, newBytes, 0, length );

	          return newBytes;
	      }
	      else
	      {
	          return new byte[0];
	      }
	  }
	  /**
	   * <p>
	   * Removes spaces (char &lt;= 32) from start of this array, handling
	   * <code>null</code> by returning <code>null</code>.
	   * </p>
	   * Trim removes start characters &lt;= 32.
	   * 
	   * <pre>
	   *  StringUtils.trimLeft(null)          = null
	   *  StringUtils.trimLeft(&quot;&quot;)            = &quot;&quot;
	   *  StringUtils.trimLeft(&quot;     &quot;)       = &quot;&quot;
	   *  StringUtils.trimLeft(&quot;abc&quot;)         = &quot;abc&quot;
	   *  StringUtils.trimLeft(&quot;    abc    &quot;) = &quot;abc    &quot;
	   * </pre>
	   * 
	   * @param bytes
	   *            the byte array to be trimmed, may be null
	   * @return the position of the first byte which is not a space, or the last
	   *         position of the array.
	   */
	  private static int trimLeft(byte[] bytes, int pos)
	  {
	      if ( bytes == null )
	      {
	          return pos;
	      }

	      while ( ( pos < bytes.length ) && ( bytes[pos] == ' ' ) )
	      {
	          pos++;
	      }

	      return pos;
	  }
	  /**
	   * <p>
	   * Removes spaces (char &lt;= 32) from end of this array, handling
	   * <code>null</code> by returning <code>null</code>.
	   * </p>
	   * Trim removes start characters &lt;= 32.
	   * 
	   * <pre>
	   *  StringUtils.trimRight(null)          = null
	   *  StringUtils.trimRight(&quot;&quot;)            = &quot;&quot;
	   *  StringUtils.trimRight(&quot;     &quot;)       = &quot;&quot;
	   *  StringUtils.trimRight(&quot;abc&quot;)         = &quot;abc&quot;
	   *  StringUtils.trimRight(&quot;    abc    &quot;) = &quot;    abc&quot;
	   * </pre>
	   * 
	   * @param bytes
	   *            the chars array to be trimmed, may be null
	   * @return the position of the first char which is not a space, or the last
	   *         position of the array.
	   */
	  private static int trimRight(byte[] bytes, int pos)
	  {
	      if ( bytes == null )
	      {
	          return pos;
	      }

	      while ( ( pos >= 0 ) && ( bytes[pos] == ' ' ) )
	      {
	          pos--;
	      }

	      return pos;
	  }
	  /**
	   * Checks if a bytes array is empty or null.
	   * 
	   * @param bytes
	   *            The bytes array to check, may be null
	   * @return <code>true</code> if the bytes array is empty or null
	   */
	  private static boolean isEmpty(byte[] bytes)
	  {
	      return bytes == null || bytes.length == 0;
	  }
	}



