package io.milton.dns.utils;

import io.milton.dns.Name;
import io.milton.dns.TextParseException;
import io.milton.dns.record.RRset;
import io.milton.dns.record.Record;
import io.milton.dns.resource.DomainResourceRecord;
import io.milton.dns.resource.DomainResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;



public class Utils {

	public static Name stringToName(String s) throws TextParseException {		
		if ( s == null ) {
			return null;
		}
		if ( !s.endsWith(".") ) {
			s += ".";
		}
		return Name.fromString(s);
	}
	
	public static String nameToString(Name n) {
		
		if ( n == null ) {
			return null;
		}
		String s = n.toString();
		if ( s.endsWith(".")){
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}
	
	/**
	 * Just for fun...
	 * @return
	 */
	public static InetAddress probeIp(){
		
		String[] urls = {"http://automation.whatismyip.com/n09230945.asp",
				"http://api.externalip.net/ip/"};
		for (String s : urls) {
			
			try {
				URL url = new URL(s);
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				conn.setRequestProperty("User-Agent", "Firefox/2.0");
				
				BufferedReader rdr = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "ASCII") );
				String addrS = rdr.readLine();
				InetAddress addr = InetAddress.getByName(addrS);
				return addr;
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch(UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	
	}
}
