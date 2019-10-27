package lucene4ir.parse;

import java.io.*;
import java.util.*;

public class CSVParser {

   /*
   Header :
    1- Key - 8 values
    corpus - indexType - qryFilter - qryCount - model - maxResults - other - B
    2- Res File - 3 values
    resLineCount - resDocCount - limitedQueries<max
    3- ret File (Cumulative) - 5 values
    retCumulativeG , retCumulativeRValues<>0 , retCumulativeRValues=0 , retCumulativeSumR , retCumulativeAVGR
    4- ret File (Gravity) - 6 values
        B , retGravityG , retGravityRValues<>0 , retGravityRValues=0 , retGravitySumR , retGravityAVGR
    5- Performance - 3 values
    MAP ,  pbref , P10

*/
    // CSV Key Fields
    public HashMap <String, String> csvMap;
    String  key, res , retC , retG , performance , csvFile;

    private final String  separator = ",";

    public CSVParser ()
    {
        csvMap = new HashMap <String, String> ();
    }

    public void resetCSVValues ()
    {
        res = "";
        retC = "";
        retG = "";
        performance = "";
    }

    private String extractValue (String value , int paramCount)
    {
        String result = "" , parts[];
        int i;

        if (value.isEmpty())
            {
            for (i = 1 ; i < paramCount ; i++)
                result += " ,";
            result += " ";
            } // End if
        else
        {
            parts = value.split(separator, paramCount+1);
            result = value.replaceFirst(parts[parts.length-1],"");
            result = result.replaceFirst(separator,"");

        } // End Else
        return result;
    }
   private String removeValue (String value , String substr)
   {
       if (!value.isEmpty())
           value = value.replaceFirst (substr,"").replaceFirst(separator,"");

       return value;
   }

   public void updateCSVFile ()
   {
       String line;
       TreeMap<String,String> sortedMap = new TreeMap<String,String>(csvMap);
       // header
       line = "corpus,indexType,qryFilter,qryCount,model,maxResults,other,B"+
               "resLineCount,resDocCount,limitedQueries<max," +
               "retCumulativeG,retCumulativeRValues<>0,retCumulativeRValues=0,retCumulativeSumR,retCumulativeAVGR," +
               "B,retGravityG,retGravityRValues<>0,retGravityRValues=0,retGravitySumR,retGravityAVGR," +
               "MAP,P10,Bref\n";
       try {
           PrintWriter pr = new PrintWriter(csvFile);
           Iterator it = sortedMap.entrySet().iterator();
           pr.write(line);
           while (it.hasNext())
           {
              Map.Entry entry = (Map.Entry) it.next();
              line = entry.getKey().toString() +"," + entry.getValue().toString() + "\n";
              pr.write(line);
           } // End While
           pr.close();
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } // End Catch
   }
    public void updateCSVLine ()
    {
        String value = "" ;
        final int resCount = 3 , retCCount = 5 , retGCount = 6 , performanceCount = 3 ;
        int i;
        if (key == null || key.isEmpty())
            System.out.println("CSV Key is Empty");
        else
        {
            if (csvMap.containsKey(key))
                value = csvMap.get(key);

            if (res.isEmpty())
                res = extractValue(value, resCount);
            value = removeValue(value,res);

            if (retC.isEmpty())
                retC = extractValue(value, retCCount);
            value = removeValue(value,retC);
            if (retG.isEmpty())
                retG = extractValue(value,retGCount);
            value = removeValue(value,retG);

            if (performance.isEmpty())
                performance = extractValue(value,performanceCount);

            value = String.format("%s,%s,%s,%s",res,retC,retG,performance);
            csvMap.put(key,value);
        }
    }

    public void readFromFile (String file)
    {
        String line , parts[]  , key , value;
        int keyCount = 8;

        csvFile = file;
        if (!new File(file).exists())
            return;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.readLine();
            while ((line = br.readLine()) != null)
            {
               parts =  line.split(separator , keyCount+1);
               value = parts[keyCount];
               key = line.replace("," + value,"");
               csvMap.put(key,value);
            } // End While

        } catch (IOException e) {
            e.printStackTrace();
        } // End Catch
    }

    public void setKey (String value)
    {
        key = value;
    }
    public void setRes (String value)
    {
        res = value;
    }
    public void setRetC (String value)
    {
        retC = value;
    }
    public void setRetG (String value)
    {
        retG = value;
    }
    public void setPerformance (String value)
    {
        performance = value;
    }

    public static void main(String[] args) {
        String file = "C:\\Users\\kkb19103\\Desktop\\Temp.txt";
        CSVParser csv = new CSVParser();
        csv.readFromFile(file);
    }

}
