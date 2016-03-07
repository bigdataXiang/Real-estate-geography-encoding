package com.svail.adressmacth;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.svail.geotext.GeoQuery;
import com.svail.util.FileTool;
import com.svail.util.HTMLTool;
 

public class  Addressmatch_58{
	//将D盘中的原文件读到程序里
	public static Vector<String> readtxt(String pathname)
	{   
	   Vector<String> pois =FileTool.Load(pathname, "UTF-8");
	   for(int i=0;i<pois.size();i++)
	   {
	     System.out.println(pois.elementAt(i));
	   }
	   return pois;
    }
	
	//提取文件中<address>标签里的内容
	public static String getStrByKey(String sContent, String sStart, String sEnd) {
			String sOut ="";
			int fromIndex = 0;
			int iBegin = 0;
			int iEnd = 0;
			int iStart=sContent.indexOf("</POI>");
			if (iStart < 0) {
			  return null;
			  }
			for (int i = 0; i < iStart; i++) {
			  // 找出某位置，并找出该位置后的最近的一个匹配
			  iBegin = sContent.indexOf(sStart, fromIndex);
			  if (iBegin >= 0) 
			  {
			    iEnd = sContent.indexOf(sEnd, iBegin + sStart.length());
			    if (iEnd <= iBegin)
			    {
			      return null;
			    }
			  }
			  else 
			  {
					return sOut;
			  }
              if (iEnd > 0)
              {
			   sOut +=sContent.substring(iBegin + sStart.length(), iEnd);
			  }
			  if (iEnd > 0) 
			  {
			   fromIndex = iEnd + sEnd.length();
			  }
			}
			  return sOut;
		}
	  //将D盘中新的原文件重新读到程序里
		public static String re_readtxt(String pathname) throws IOException{   
		       File file=new File(pathname);
		       FileInputStream fis=new FileInputStream(file);
		       InputStreamReader isr=new InputStreamReader(fis,"UTF-8");
		       BufferedReader reader=null;      
		       String tempString=null;
		       String total_addr="";
		       try
		      {
			     System.out.println("以行为单位读取文件内容,一次读一行:");
			     reader = new BufferedReader(isr);  
			     while((tempString=reader.readLine())!=null)
			     {
				   System.out.println("开始提取新的原文件的地址标签:");
				   String partcontent1=getStrByKey(tempString,"<TITLE>","</TITLE>");
				   String partcontent2=getStrByKey(tempString,"<ADDRESS>","</ADDRESS>");
				   String address="北京"+partcontent1+partcontent2;
				   System.out.println(address);
				   total_addr+=address+"\r\n";
			     }
			       reader.close();
			     }catch(FileNotFoundException e)
		      {
			     e.printStackTrace();
		      }catch(IOException e)
		     {
			    e.printStackTrace();
		     }finally
		     {
			  if(reader!=null){
			  try{
					reader.close();
				 }catch(IOException e){
					e.printStackTrace();
				 }
			  }
		   }
		    return total_addr;
	  }

	
	//地址匹配程序
	public static String parseLngLat(String query) throws UnsupportedEncodingException{
		String request = "http://192.168.6.9:8080/p41?f=json";
		String parameters = "&within="
			+ java.net.URLEncoder.encode("北京市", "UTF-8")
			+ "&key=206DA5B15B5211E5BFE0B8CA3AF38727&queryStr=";
		//


		Gson gson = new Gson();
		String lnglat = "";
		String uri = null;
		try {
			uri = request + parameters+ java.net.URLEncoder.encode(query, "UTF-8");
			String xml = HTMLTool.fetchURL(uri, "UTF-8", "get");
			
			if (xml != null)
			{
				// 创建一个JsonParser
				JsonParser parser = new JsonParser();
		
				//通过JsonParser对象可以把json格式的字符串解析成一个JsonElement对象
				try {
					JsonElement el = parser.parse(xml);

					//把JsonElement对象转换成JsonObject
					JsonObject jsonObj = null;
					if(el.isJsonObject())
					{
						jsonObj = el.getAsJsonObject();
						GeoQuery gq = gson.fromJson(jsonObj, GeoQuery.class);
						
						if (gq != null && gq.getResult() != null && gq.getResult().size() > 0 && gq.getResult().get(0).getLocation() != null)
						{
							lnglat = gq.getResult().get(0).getLocation().getLng() + ";" + gq.getResult().get(0).getLocation().getLat();
							
						}
					}
					
					
				}catch (JsonSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
			return lnglat;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		return null;
	}

	//将解析的地址坐标追加到原文件中
		public static void process(String addresspath,String originalpath) throws Exception{
			File address_file=new File(addresspath);
	        File original_file=new File(originalpath);
			FileInputStream address_fis=new FileInputStream(address_file);
			FileInputStream original_fis=new FileInputStream(original_file);
			InputStreamReader address_isr=new InputStreamReader(address_fis,"UTF-8");
			InputStreamReader original_isr=new InputStreamReader( original_fis,"UTF-8");
			BufferedReader address_reader=null; 
			BufferedReader original_reader=null; 
			String orig_tempString="";
			String addr_tempString="";
			String result="";
			String outstr ="";
			try
			{
				address_reader = new BufferedReader(address_isr); 
				original_reader = new BufferedReader(original_isr); 
				while((orig_tempString=original_reader.readLine())!=null&&(addr_tempString=address_reader.readLine())!=null)
				{
					//用insert()方法将坐标插入记录中
					String coordinate=parseLngLat(addr_tempString);
					int n = orig_tempString.indexOf("﻿<POI>");
					StringBuffer orig_buffer = new StringBuffer(orig_tempString);
					//使用insert()函数将一个字符串插入到另一个字符串中
					StringBuffer result_buffer = orig_buffer.insert(n+"<POI>".length()+1,"<coordinate>"+coordinate+"</coordinate>");//"<coordinate>"++"</coordinate>"
					System.out.println(result_buffer.toString());
					write(result_buffer.toString(),"E:/crawldata_beijing/58/rentout/4_58_rentout_result.txt");
					//result+=result_buffer.toString()+"\r\n";//
				}
				
				original_reader.close();
				address_reader.close();
			}catch(FileNotFoundException e)
		    {
				e.printStackTrace();
			}catch(IOException e)
			{
				e.printStackTrace();
			}finally
			{
				if(original_reader!=null&&address_reader!=null){
			try{
				original_reader.close();
				address_reader.close();
				}catch(IOException e){
				e.printStackTrace();
				}
			  }
			}
			
		}
	
	//将处理完的文件又重新写到D盘里
	public static void write(String writeTxt,String pathname)  throws IOException
	{
		try
		{
          File writefile=new File(pathname);
          if(!writefile.exists())
          {
        	  writefile.createNewFile();
          }
          OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(writefile,true),"UTF-8");
          BufferedWriter writer = new BufferedWriter(write);
          writer.write(writeTxt);
          writer.write("\r\n");
          writer.close();
          }catch(Exception e) {
			e.printStackTrace();
		
		}	
	}
	public static void re_write(String writeTxt,String pathname)  throws IOException
	{
		try
		{
		
          File writefile=new File(pathname);
          if(!writefile.exists())
          {
        	  writefile.createNewFile();
          }
          OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(writefile),"UTF-8");
          BufferedWriter writer = new BufferedWriter(write);
          writer.write(writeTxt+"</POI>");
          writer.write("\r\n"); 
          writer.close();
          }catch(Exception e) {
			e.printStackTrace();
		
		}	
	}
	
		 public static void main(String argv[]) throws Exception{
		        //调用读取原文件 的函数
			    //String addr=parseLngLat("北京上庄家园上庄家园西路");
			   // System.out.println(addr);
			 /*
			    System.out.println("开始读取原文件:");
			    Vector<String> content=readtxt("D:/58/resold/1_58_resold.txt");
			    int n=0;
				String poi="";
				n=content.size();
				for(int m=0;m<n;m++)
				{
					System.out.println(content.elementAt(m));
				    poi+=content.elementAt(m);
				}
				System.out.println("将vector里的所有的元素加起来组成一个字符串:");
				System.out.println(poi);
				System.out.println("将组成的字符串进行分割:");
				String [] result = poi.split("</POI>");
				for(int a = 0;a<result.length;a++){
				   System.out.print(result[a]+"\r\n");
				 }
				System.out.println("*************************************************************");
				//将整理好的原文件重新存入新的文档中
				write(result,"D:/58/resold/58_resold_new.txt");
				//重新读取新的原文件,并提取原文件中含有地址标签的字符串
				 * */
				 
				//System.out.println("重新读取新的原文件:");
				//String re_content=re_readtxt("D:/58/newhouse/2_58_newhouse.txt");
				//write(re_content,"D:/58/newhouse/2_58_newhouse_address.txt");
				//System.out.println("*************************************************************");
				//从新的文件中再次读取提取出来的地址字符串和原始文件,逐行读取,方便逐行调用地址解析的函数,逐行解析出坐标后再插入到原文件中
				System.out.println("开始将地址解析后的坐标追加到新的原文件中:");
				process("E:/crawldata_beijing/58/rentout/4_58_rentout_address.txt","E:/crawldata_beijing/58/rentout/4_58_rentout.txt");
				//E:\crawldata_beijing\58\rentout
				
				
			
	     }
		
}
	
