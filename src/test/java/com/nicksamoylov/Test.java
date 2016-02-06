package com.nicksamoylov;

import java.util.HashMap;
import java.util.Map;

public class Test {
	public static void main(String[] args) {
		Color c = Color.WHITE;
		if(c==Color.BLACK){
			System.out.println("c="+c);
		}
		else {
			System.out.println("c="+c);
		}
	}
    private static void arrayIter(){
		String[][] a = new String[3][2];
		a[0][0] = "key1";
		a[0][1] = "v1";
		a[1][0] = "key2";
		a[1][1] = "v2";
		a[2][0] = "key3";
		a[2][1] = "v3";
		for(String b[]:a){
			System.out.println("b.length="+b.length);
			for(String c:b){
				System.out.println("c="+c);
			}
		}
    }
    private static void math(){
		float a = 2.92f;
		float b = 2.22f;
		int i = Math.round(a);
		System.out.println("i="+i);
		i = Math.round(b);
		System.out.println("i="+i);
    }
    private static void zipcode(){
    	String z = "501";
    	while(z.length() < 5){
    		z = "0"+z;
    	}
		System.out.println("z="+z);
    }
}
