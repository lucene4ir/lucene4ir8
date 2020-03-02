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
   // public CSVLine csvLine;
    // Res - Ret - Per
    String csvType , csvFile;

    private final String separator = ",";

    public CSVParser(String csvPath , String inCsvType) {
        csvMap = new HashMap<String, String>();
        csvType = inCsvType.toLowerCase();
        csvFile = csvPath + "\\" + inCsvType + ".csv";
        readFromFile(csvFile);
    }

    private String getHeader ()
    {
        String keyHeader = "corpus,indexType,qryFilter,qryCount,model,maxResults,RetrievalCoefficient,";
        String result = "";
        switch (csvType)
        {
            case "res":
                result = keyHeader + "resLineCount,resDocCount,limitedQueries<max";
                break;
            case "ret":
                keyHeader = "corpus,indexType,qryFilter,qryCount,model,maxResults,RetrievabilityB,RetrievalCoefficient,";
                result = keyHeader + "G,ZeroRValuesCount,rSum";
                break;
            case "per":
                result = keyHeader + "MAP,Bref,P10,RBP0.6,RBP0.8";
                break;
        } // End Switch
      return result + "\n";
    } // End Function getHeader

    public void updateCSVFile() {
        String line;
        try {
            PrintWriter pr = new PrintWriter(csvFile);
            Iterator it = csvMap.entrySet().iterator();
            line = getHeader();
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

    public void addCSVLineToMap(String inKey , String inValue) {
        csvMap.put(inKey,inValue);
    }

    public void readFromFile(String file) {
        /*
        Read All CSV Lines from The input File
        Split Lines into (Key , Value ) and put them in CSV HashMap
         */
        String line, parts[], key, value;
        int keyCount;
        if (csvType.equals("ret"))
            keyCount = 8;
        else
            keyCount = 7;

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
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } // End Catch
    } // End Function readFromFile
}