package lucene4ir.parse;

import sun.invoke.empty.Empty;

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
    public HashMap<String, String> csvMap;
    public CSVLine newLine;
    String csvFile;

    private final String separator = ",";

    public CSVParser() {
        csvMap = new HashMap<String, String>();
        newLine = new CSVLine();
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


  /*  private boolean isEmpty (String value)
    {
        *//*
        isEmpty = true if :
        value is Empty
        All Separated values are Empty
        *//*

        boolean result = true;
        String parts[];
        if (!value.isEmpty())
        {
            parts = value.split(separator);
            for (int i = 0 ; i < parts.length ; i++)
                    if (!parts[i].isEmpty())
                    {
                        result = false;
                        break;
                    } // End if
        } // End if
        return result;
    }*/

    public void updateCSVFile() {
        String line;
        TreeMap<String, String> sortedMap = new TreeMap<String, String>(csvMap);
        // header
        line = "corpus,indexType,qryFilter,qryCount,model,maxResults,other,B" +
                "resLineCount,resDocCount,limitedQueries<max," +
                "retCumulativeG,retCumulativeRValues<>0,retCumulativeRValues=0,retCumulativeSumR,retCumulativeAVGR," +
                "B,retGravityG,retGravityRValues<>0,retGravityRValues=0,retGravitySumR,retGravityAVGR," +
                "MAP,P10,Bref\n";
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

    public String updateCSVLine() {
        String value = "" , newKey , result = "";
        final int  resCount = 3, retCCount = 5, retGCount = 6, performanceCount = 3;
        int start;

        newKey = newLine.getKey();
        if (newKey == null || newKey.isEmpty())
            System.out.println("CSV Key is Empty");
        else {
            if (csvMap.containsKey(newKey))
                value = csvMap.get(newKey);

            // Res
            start = 0;
            if (newLine.getRes().isEmpty())
                newLine.setRes(extractValue(value,start,resCount));

            // Retrievability Cumulative
            start += resCount;
            if (newLine.getRetCG().isEmpty())
                newLine.setRetCG(extractValue(value,start,retCCount));

            // Retrievability Gravity
            start += retCCount;
            if (newLine.getRetGG().isEmpty())
                newLine.setRetGG(extractValue(value,start,retGCount));

            start +=retGCount;
            if (newLine.getPerformance().isEmpty())
                newLine.setPerformance(extractValue(value,start,performanceCount));

            value = newLine.getAllValues();


            csvMap.put(newKey, value);
            value = newKey + separator + value;
        } // End Else
        return value;
    }

    public void readFromFile(String file) {
        String line, parts[], key, value;
        int keyCount = 8;

        csvFile = file;
        if (!new File(file).exists())
            return;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.readLine();
            while ((line = br.readLine()) != null) {
                parts = line.split(separator, keyCount + 1);
                value = parts[keyCount];
                key = line.replace("," + value, "");
                csvMap.put(key, value);
            } // End While

        } catch (IOException e) {
            e.printStackTrace();
        } // End Catch
    } // End Function readFromFile


    public void resetValues()
    {
        newLine.resetValues();
    }

    public void appendPerformance (String performance)
    {
        newLine.setPerformance(performance);
    }

    public void appendRes (String res)
    {
        newLine.setRes(res);
    }

    public void appendRetCG (String RetCG)
    {
        newLine.setRetCG(RetCG);
    }

    public void appendRetGG (String RetGG)
    {
        newLine.setRetGG(RetGG);
    }

    public void appendKey (String key)
    {
        newLine.setKey(key);
    }
}

class CSVLine {
    public String res,retCG,retGG , performance, key;

    public void resetValues ()
    {
        setRes("");
        setRetCG("");
        setRetGG("");
        setPerformance("");
    }

    public String getAllValues()
    {
        return String.format("%s,%s,%s,%s", res, retCG, retGG, performance);
    }
    public String getRetCG() {
        return retCG;
    }

    public void setRetCG(String retCG) {
        this.retCG = retCG;
    }
    public String getRetGG() {
        return retGG;
    }
    public void setRetGG(String retGG) {
        this.retGG = retGG;
    }

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getKey()
    {
        return key;
    }
}