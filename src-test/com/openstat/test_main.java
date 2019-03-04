package com.openstat;

import java.net.UnknownHostException;

public class test_main {
      public static void main(String[] arg) throws UnknownHostException {
    	  IPv6RadixIntTree tr = new IPv6RadixIntTree();
//    	      tr.put("FF05::B3/24");
    	  tr.put("FF05::B3/24",43);
    	  tr.put("FF02::B3", "FFFF::", 42);
//    	  IPv4RadixIntTree tr = new IPv4RadixIntTree();
//    	  tr.put(0x0a000000, 0xffffff00, 42);
//    	  tr.put(0x0a000000, 0xff000000, 69);
//    	  tr.put("10.0.3.0/24", 123);
    	  int v1 = tr.selectValue("FF05::"); // => 69, as 10.32.32.32 belongs to 10.0.0.0/8
    	  int v2 = tr.selectValue("FF02::B2"); // => 42, as 10.0.0.32 belongs to 10.0.0.0/24
//    	  int v3 = tr.selectValue("10.0.3.5"); // => 123, as 10.0.3.5 belongs to 10.0.3.0/24
    	  System.out.println(v1);
    	  System.out.println(v2);
      }
}
