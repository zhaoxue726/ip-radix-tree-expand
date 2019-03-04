package com.openstat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPv6RadixIntTree {
	public static final int NO_VALUE = -1;

    private static final int NULL_PTR = -1;
    private static final int ROOT_PTR = 0;

    private static final long MAX_IPV4_BIT = 0x80000000L;

    private int[] rights;
    private int[] lefts;
    private int[] values;

    private int allocatedSize;
    private int size;
    
    public IPv6RadixIntTree() {
        init(1024);
    }
    public IPv6RadixIntTree(int allocatedSize) {
        init(allocatedSize);
    }
    private void init(int allocatedSize) {
        this.allocatedSize = allocatedSize;

        rights = new int[this.allocatedSize];
        lefts = new int[this.allocatedSize];
        values = new int[this.allocatedSize];

        size = 1;
        lefts[0] = NULL_PTR;
        rights[0] = NULL_PTR;
        values[0] = NO_VALUE;
    }
   public void put(String ipNet,String mask,int value) throws UnknownHostException {
		String ipNetString = InetAddress.getByName(ipNet).getHostAddress();
       	String maskString = InetAddress.getByName(mask).getHostAddress();
       	String[] ipNetArray = ipNetString.split(":");
       	String[] maskArray = maskString.split(":");
       	long[] Keys = new long[4] ;
       	long[] Masks = new long[4];
    	    for(int i=0;i<8;i++) {
       		ipNetArray[i] = String.format("%4s",ipNetArray[i]);
       		ipNetArray[i] = ipNetArray[i].replaceAll(" ","0");
            maskArray[i] = String.format("%4s",maskArray[i]);
            maskArray[i] = maskArray[i].replaceAll(" ","0");
       	}
       	for(int i=0,j=0;i<4;i++) {
       		ipNetArray[i]=ipNetArray[j]+ipNetArray[j+1];
       		maskArray[i]=maskArray[j]+maskArray[j+1];
       		Keys[i]= Long.valueOf(ipNetArray[i],16);
       		Masks[i]= Long.valueOf(maskArray[i],16);
       		j=j+2;
       	}
       	store(Keys,Masks,value);
   }
   public void put(String ipNet,int value) throws UnknownHostException {
      	int pos = ipNet.indexOf('/');
      	String ipStr = ipNet.substring(0, pos);
   	    String ipNetString = InetAddress.getByName(ipStr).getHostAddress();
   	    String netmaskStr = ipNet.substring(pos + 1);
       	String[] ipNetArray = ipNetString.split(":");
   	    int cidr = Integer.parseInt(netmaskStr);

   	    long[] Keys = new long[4];
   	    for(int i=0;i<8;i++) {
      		ipNetArray[i] = String.format("%4s",ipNetArray[i]);
      		ipNetArray[i] = ipNetArray[i].replaceAll(" ","0");
      	}
      	for(int i=0,j=0;i<4;i++) {
      		ipNetArray[i]=ipNetArray[j]+ipNetArray[j+1];
      		Keys[i]= Long.valueOf(ipNetArray[i],16);
      		j=j+2;
      	}
      	long[] Masks = new long[4];

       int j=0;
       	for(j=0;j<cidr/32;j++) {
       		Masks[j]=0xffffffffL;
       	}
       	if(cidr%32!=0) {
       		cidr=cidr%32;
       		Masks[j]=((1L << (32 - cidr)) - 1L) ^ 0xffffffffL;
       		j++;
       	}
       	while(j<4) {
       		Masks[j]=0;
       		j++;
       	}
   	   store(Keys,Masks,value);
   	    
   }
    public void store(long[] Keys,long[] Masks,int value){
       
       	long bit = MAX_IPV4_BIT;
        int node = ROOT_PTR;
        int next = ROOT_PTR;
       	int i=0;
       	while ((bit & Masks[i]) != 0) {
                
                next = ((Keys[i] & bit) != 0) ? rights[node] : lefts[node];
                if (next == NULL_PTR) {
                    break;}

                
                bit >>= 1;
       	        if(bit==0 && i<3) {
       	    	        bit=MAX_IPV4_BIT;
       	    	        i++;
       	       }
                node = next;
            }

            if (next != NULL_PTR) {

                values[node] = value;
                return;
            }

            while ((bit & Masks[i]) != 0) {
                if (size == allocatedSize)
                    expandAllocatedSize();

                next = size;
               
                values[next] = NO_VALUE;
                rights[next] = NULL_PTR;
                lefts[next] = NULL_PTR;

                if ((Keys[i] & bit) != 0) {
                    rights[node] = next;
                } else {
                    lefts[node] = next;
                }

                bit >>= 1;
                if(bit==0 && i<3) {
   	    	        bit=MAX_IPV4_BIT;
   	    	        i++;
   	       }
                node = next;
                size++;
            }

            values[node] = value;
       	

       	values[node] = value;
    }


    public int selectValue(String ipNet) throws UnknownHostException {
       	String ipNetString = InetAddress.getByName(ipNet).getHostAddress();
       	String[] ipNetArray = ipNetString.split(":");
       	long[] Keys = new long[4];
       	
       	for(int i=0;i<8;i++) {
       		ipNetArray[i] = String.format("%4s",ipNetArray[i]);
       		ipNetArray[i] = ipNetArray[i].replaceAll(" ","0");
       	}
       	for(int i=0,j=0;i<4;i++) {
       		ipNetArray[i]=ipNetArray[j]+ipNetArray[j+1];
       		Keys[i]= Long.valueOf(ipNetArray[i],16);
       		j=j+2;
       	}
        long bit = MAX_IPV4_BIT;
        int value = NO_VALUE;
        int node = ROOT_PTR;

        int i=0;
        while (node != NULL_PTR) {
            if (values[node] != NO_VALUE)
                value = values[node];
            node = ((Keys[i] & bit) != 0) ? rights[node] : lefts[node];
            bit >>= 1;
       	    if(bit==0) {
       	    	    bit=MAX_IPV4_BIT;
       	    	    i++;
       	    }
        }

        return value;
    }

    private void expandAllocatedSize() {
        int oldSize = allocatedSize;
        allocatedSize = allocatedSize * 2;

        int[] newLefts = new int[allocatedSize];
        System.arraycopy(lefts, 0, newLefts, 0, oldSize);
        lefts = newLefts;

        int[] newRights = new int[allocatedSize];
        System.arraycopy(rights, 0, newRights, 0, oldSize);
        rights = newRights;

        int[] newValues = new int[allocatedSize];
        System.arraycopy(values, 0, newValues, 0, oldSize);
        values = newValues;
    }
    
}
