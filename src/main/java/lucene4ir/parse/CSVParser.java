package lucene4ir.parse;

import java.io.*;
import java.util.*;

public class CSVParser {

    /*
    Header :
     1- Key - 8 values
     corpus - indexType - qryFilter - qryCount - model - maxResults - RetrievabilityB - RetrievalCoefficient (B , mu , c)
     2- ret File - 3 values
        G , ZeroRValuesCount , SumR
     3- res File - 3 values
        resLineCount - resDocCount - limitedQueries<max
     4- Performance - 3 values
        MAP ,  pbref , P10

 */
    // CSV Key Fields
    public HashMap<String, String> csvMap;
    public CSVLine csvLine;
    String csvFile;

    private final String separator = ",";

    public CSVParser() {
        csvMap = new HashMap<String, String>();
        csvLine = new CSVLine();
    }

    private String extractValue(String csvValues, int start ,  int paramCount) {
       /*
       Extract Specific subset Of Values from a given csvValues of one line
        if csvValues is Empty - return empty separated values
        else
        Extract the Existing values Separated By Comma
        */
        String result = "", parts[];
        int end;

        if (csvValues.isEmpty()) {
            for (int i = 1; i < paramCount; i++)
                result += " ,";
            result += " ";
        } // End if
        else {
            end = start + paramCount;
            parts = csvValues.split(separator, end + 1);
            for (int i = start ; i < end ; i++)
                result += separator + parts[i] ;
            result = result.replaceFirst(separator,"");
        } // End Else
        return result;
    }

    public void updateCSVFile() {
        String line;
        TreeMap<String, String> sortedMap = new TreeMap<String, String>(csvMap);
        // header
        line = "corpus,indexType,qryFilter,qryCount,model,maxResults,RetrievabilityB,RetrievalCoefficient," +
                "G,ZeroRValuesCount,rSum," +
                "resLineCount,resDocCount,limitedQueries<max," +
                "MAP,Bref,P10\n";
        try {
            PrintWriter pr = new PrintWriter(csvFile);
            Iterator it = sortedMap.entrySet().iterator();
            pr.write(line);
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                line = entry.getKey().toString() + "," + entry.getValue().toString() + "\n";
                pr.write(line);
            } // End While
            pr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } // End Catch
    }

    public String addCSVLineToMap() {
        String value = "" , newKey , result = "";
        int  retCount = 3 , resCount = 3 , performanceCount = 3 ;
        int start;

        newKey = csvLine.getKey();
        if (newKey == null || newKey.isEmpty())
            System.out.println("CSV Key is Empty");
        else {
            if (csvMap.containsKey(newKey))
                value = csvMap.get(newKey);

            // Retrievability Cumulative
            start = 0;
            if (csvLine.getRet().isEmpty())
                csvLine.setRet(extractValue(value,start,retCount));

            // Res
            start += retCount;
            if (csvLine.getRes().isEmpty())
                csvLine.setRes(extractValue(value,start,resCount));


            start +=resCount;
            if (csvLine.getPerformance().isEmpty())
                csvLine.setPerformance(extractValue(value,start,performanceCount));

            value = csvLine.getAllValues();


            csvMap.put(newKey, value);
            value = newKey + separator + value;
            System.out.println("Added CSV Line : " + value);
        } // End Else
        return value;
    }

    public void readFromFile(String file) {
        /*
        Read All CSV Lines from The input File
        Split Lines into (Key , Value ) and put them in CSV HashMap
         */

        String line, parts[], key, value;
        int keyCount = 8;

        csvFile = file;
        if (!new File(file).exists())
            return;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.readLine();
            while ((line = br.readLine()) != null) {
                parts = line.split(separator, keyCount  + 1);
                value = parts[keyCount];
                key = line.replace("," + value, "");
                csvMap.put(key, value);
            } // End While

        } catch (IOException e) {
            e.printStackTrace();
        } // End Catch
    } // End Function readFromFile

    public void setPerformance (String performance)
    { csvLine.setPerformance(performance); }

    public void setRes(String res)
    { csvLine.setRes(res); }

    public void setRet(String ret)
    { csvLine.setRet(ret); }

    public void setKey (String key)
    { csvLine.setKey(key); }

    public String getKey ()
    { return csvLine.getKey(); }

    public void newLine(String key)
    { // Append NewCSV Line
        csvLine.setKey(key);
        csvLine.resetValues();
    }
}

class CSVLine {
    // public String res,retCG,retGG , performance, key;
    // Parameter Groups in CSV Line
    public String key = "" , ret = "" , res = "" , performance = "";

    public void resetValues ()
    {
        setRes("");
        setRet("");
        setPerformance("");
    }

    public String getAllValues()
    {
        return String.format("%s,%s,%s", ret , res , performance );
    }


    public void setPerformance(String performance) { this.performance = performance; }
    public String getPerformance() { return performance; }

    public void setRes(String res) { this.res = res; }
    public String getRes() {
        return res;
    }

    public void setRet(String ret) { this.ret = ret; }
    public String getRet() {
        return ret;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getKey()
    {
        return key;
    }
}
