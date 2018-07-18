import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.*;

public class Main {

	public static void main(String [ ] args) throws ParseException, IOException
	{
		String t = "";
		//String testhl = "HomeLoanService/HomeLoanWS/doSubmit/approve-060/partial-installment";
		//String test2slash ="CommonService/UnsecureWS/login";
		//String testhaveid = "CommonService/dropdownWS/getDropdown/HL_GENDER";
		logAna("fls2-service-log_"+t,"F:\\fls2_log\\");
		/*
		System.out.println(subSrtingIdUrlNotHomeLoanWS(testhl));
		System.out.println(subSrtingIdUrlNotHomeLoanWS(test2slash));
		System.out.println(subSrtingIdUrlNotHomeLoanWS(testhaveid));
		*/
	}

	public static void logAna(String file_name,String Path) throws IOException, ParseException {
		List<String> stimeList = new ArrayList<>();
		List<String> sUrlList = new ArrayList<>();
		List<String> list = new ArrayList<>();
		//List<String> serviceList = new ArrayList<>();
		File file = new File(Path);
		File[] files = file.listFiles();
		for(File f: files){
		//for(int i =0;i<1;i++){
			try{
				//PrintWriter writer = new PrintWriter(newFile+".txt", "UTF-8");
				
			   FileInputStream fstream = new FileInputStream(f);
			   System.out.println(f);
			   BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			   String strLine;
			   String svTime;
			   String svUrl;
			   String svDate;
			  
			   /* read log line by line */
			   while ((strLine = br.readLine()) != null)   {
			     if(serviceLogLine(strLine)) {
					   //writer.println(strLine);
			    	 svDate = between2String(strLine,"INFO |",",");
			    	 svTime = between2String(strLine,"[IN FLS2 - "," ms]");
					 svUrl = strLine.substring(strLine.lastIndexOf("/services/")+10);
					 //skip 17
					   if(!svTime.equals("")) {
						   if(NotInDatetime(svDate)) {
							   stimeList.add(svDate+","+svTime);
							   sUrlList.add(svUrl.replaceFirst("/", ","));
							   //serviceList.add(svDate+","+svTime+","+svUrl);
							   list.add(subSrtingIdUrlNotHomeLoanWS(svUrl)+","+svTime);
						   }
					   }
			     }
			   }
			   
			   Collections.sort(list);
			   fstream.close();
			   
			} catch (Exception e) {
			     System.err.println("Error: " + e.getMessage());
			}
        }
		if(!list.isEmpty()) {
			//avg service
			int count = 0;
			List<String> avglist = new ArrayList<>();
			String svtmp;
			//System.out.println(list.toString());
			String[] datalog = list.get(0).split(",");
			String service_name=datalog[0];
			int sum_time = 0;
			int min_time = Integer.MAX_VALUE;
			int max_time = Integer.MIN_VALUE;
			int avg_time = 0;
			int axavg_time =0;
			int timetmp = 0;
			for(int i=0;i<list.size();i++) {
				datalog = list.get(i).split(",");
				svtmp = datalog[0];
				timetmp = Integer.parseInt(datalog[1]);
				if(!service_name.equals(svtmp)) {
					avg_time = sum_time/count;
					axavg_time = avg_time*count;
					avglist.add(max_time+","+min_time+","+avg_time+","+count+","+axavg_time+","+service_name.replaceFirst("/", ","));
					service_name = svtmp;
					sum_time = 0;
					count=0;
					min_time = Integer.MAX_VALUE;
					max_time = Integer.MIN_VALUE;
				}
				if(min_time > timetmp)
					min_time  = timetmp;
				if(max_time < timetmp)
					max_time = timetmp;
				sum_time += timetmp;
				count++;
			}
			avglist.add(max_time+","+min_time+","+avg_time+","+count+","+axavg_time+","+service_name.replaceFirst("/", ","));
			writeAnalyzedFile("analyzed-"+file_name+java.time.LocalDate.now(),avglist);
			
			//System.out.println(serviceList");
			writeFile("all-data-"+file_name+java.time.LocalDate.now(),stimeList, sUrlList);
		}
		else {
			System.out.println("Empty");
		}
		
		
	}
	private static boolean NotInDatetime(String svDate) throws ParseException {
		return 	NotInDateTime(svDate, "2000-07-05 00:00:00", "2018-07-16 12:59:00");
				//&& NotInDateTime(svDate, "2018-07-05 17:53:00", "2018-07-05 18:25:00") 
				//&& NotInDateTime(svDate, "2018-07-06 18:45:00", "2018-07-06 19:01:00");
	}
	
	private static String subSrtingIdUrlNotHomeLoanWS(String url) {
		String[] urlArray = url.split("/");
		if(!(urlArray.length==0)) {
			if(!(urlArray[0].equals("HomeLoanService"))) {
				//System.out.println(url);
				if(urlArray.length>2) {
					return urlArray[0]+"/"+urlArray[1]+"/"+urlArray[2];
				} else {
					return urlArray[0]+"/"+urlArray[1];
				}
				
			}
			else 
				return url;
		}
		else return "Err";
	}
	
	private static boolean NotInDateTime(String date,String dateTimeStart, String dateTimeEnd) throws ParseException {
		// TODO Auto-generated method stub
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateS = formatter.parse(dateTimeStart);
		Date dateE = formatter.parse(dateTimeEnd);
		Date dateC = formatter.parse(date);
		return !(dateC.after(dateS) && dateC.before(dateE));
		
	}
	public static boolean serviceLogLine(String str) {
		//greater than .. sec
		//Pattern pattern = Pattern.compile(".*2018-07-[0-9][4-9].*service.*[2-9]\\d{3} ms\\].*");
		//All
		Pattern pattern = Pattern.compile(".*2018-07-[1-9][0-9].*service.* ms\\].*");
		Matcher matcher	= pattern.matcher(str);
		
		return matcher.matches();
		
	}
	
	public static String between2String(String strline,String p1,String p2) {
		String result="";
		Pattern p = Pattern.compile(Pattern.quote(p1) + "(.*?)" + Pattern.quote(p2));
		Matcher m = p.matcher(strline);
		if(m.find()) {
			result = m.group(1);
		}
		return result;
				
	}

	public static void writeFile(String filename,List<String> col1,List<String> col2) throws FileNotFoundException{
        PrintWriter pw = new PrintWriter(new File(filename+".csv"));
        StringBuilder sb = new StringBuilder();
      //head .csv
        sb.append("Log Time,Period(ms),Service,Operation");
        sb.append('\n');
        for(int i =0; i<col1.size();i++) {
        	//System.out.println(col1.get(i)+"----"+col2.get(i));
        	sb.append(col1.get(i));
            sb.append(',');
            sb.append(col2.get(i));
            sb.append('\n');
        }

        pw.write(sb.toString());
        pw.close();
        System.out.println("done!");
    }
	
	public static void writeAnalyzedFile(String filename,List<String> col1) throws FileNotFoundException{
		File csvfile = new File(filename+".csv");
        PrintWriter pw = new PrintWriter(csvfile);
        StringBuilder sb = new StringBuilder();
        //head .csv
        sb.append("MaxPeriod(ms),MinPeriod(ms),AvgPeriod(ms),Amount,AxAvg,Service,Operation");
        sb.append('\n');
        for(int i =0; i<col1.size();i++) {
        	sb.append(col1.get(i));
            sb.append('\n');
        }

        pw.write(sb.toString());
        pw.close();
        System.out.println("Analyzed done!");
    }
	
	
}
