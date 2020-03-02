package lucene4ir;

import lucene4ir.Stats.StatisticsRetrievabilityCalculator;
import lucene4ir.Stats.StatisticsRetrieval;
import lucene4ir.parse.CSVParser;

import lucene4ir.utils.CrossDirectoryClass;
import lucene4ir.utils.XMLTextParser;

import javax.xml.bind.JAXB;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class RunExperimentSet {
    private ExperimentSetParams p;

    private String
           /* biFilter = "params/index/p.tokenFilterFile_Bigram.xml",
            uniFilter = "params/index/p.tokenFilterFile_Unigram.xml" ,
            combinedFilter = "params/index/p.tokenFilterFile_Combinedgram.xml" ,
            tokenFilterFile = combinedFilter,*/
            defaultCSVKey , filePrefix;
    private CSVParser csvPaserPer , csvPaserRes , csvPaserRet;
    private  HashMap<String, Double> initMap;
    private String getCorpus (String indexName)
    {
        String result = "";

        if (!indexName.isEmpty())
            if (indexName.contains("Core17"))
                result = "Core17";
            else  if (indexName.contains("WAPO"))
                result = "WAPO";
            else
                result = "Aquaint";
        return result;
    } // End Function


    private String getBashLine(String b)
    {
        String qrelFile , trecEvalLine = "", corpus;

        if (!validRetrievability())
        {
            corpus = getCorpus(p.indexName);
            switch (corpus)
            {
                case "Core17":
                    qrelFile = "307-690.qrels";
                    break;
                case "WAPO":
                    qrelFile = "qrels.core18.txt";
                    break;
                default:
                    qrelFile = "trec2005.aquaint.qrels";
            } // End switch
            trecEvalLine  = String.format("~/trecEval/trec_eval ~/trecEval/Qrels/%s ./result%s.res > ./trec%s.trec\n" ,
                    qrelFile,b,b);
        } // End if (!validRetrievability())
        return trecEvalLine;
    } // End Function

    private void fillAutoParameters()
    {
        /*
        Fill Parameters Automatically
        Fill QueryFile based on Max Results
        if Max Results = 1000 - Query 50
        else Query 300K
        + fill The outputDir Automatically based on MaxResults & IndexName
         Sample OutputDir
         out\Core17\UnigramIndex\50\C1000\CombinedgramFilter
         */
        String qryCount = "" , indexFolder = "" , C = "", corpus , outPutFolder,csvPath;

        if (p.mainDir.isEmpty())
            outPutFolder = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments\\WAPO 26-02-2020";
        else
            outPutFolder = p.mainDir;


        if (validRetrievability() &&
                initMap == null &&
                p.exType.contains("All")
        )
            cloneMap();
        C = "C" + p.maxResults;
        if (validRetrievability())
            qryCount = "300K";
        else
            qryCount = "50";

        corpus = getCorpus(p.indexName);
        p.queryFile = String.format("%s/Queries/%s.qry" , outPutFolder, corpus , qryCount);

        if (p.indexName.contains("Combined"))
            indexFolder = "CombinedIndex";
        else if (p.indexName.contains("Unigram"))
            indexFolder = "UnigramIndex";
        else if (p.indexName.contains("Bigram"))
            indexFolder = "BigramIndex";
        else if (p.indexName.contains("Fielded"))
            indexFolder = "FieldedIndex";

        //   Sample Output
        //   \ExperimentFolder\WA-BM25-BI-50-C1000-b0.6.res
        filePrefix = String.format("%s\\ExperimentFolder\\%s-%s-%s-%s-%s-" ,
                outPutFolder , corpus.substring(0,2) ,
                p.model , indexFolder.substring(0 ,1) + "I"
                , qryCount , C);

         /*CSV Key
        corpus - indexType - qryFilter - qryCount - model - maxResults - RetrievabilityB - RetrievalCoefficient (B , mu , c) */
        defaultCSVKey = String.format("%s,%s,%s,%s,%s,%s",
                corpus,indexFolder , "combinedQuery" , qryCount , p.model , p.maxResults);

        p.indexName = outPutFolder + "\\Indexes\\" + p.indexName;
        if (p.csvPath.isEmpty())
            csvPath = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments\\CSV";
        else
            csvPath = p.csvPath;
        switch (p.exType)
        {
            case "All":
                csvPaserRes = new CSVParser(csvPath,"Res");
                csvPaserRet = new CSVParser(csvPath,"Ret");
                break;
            case "AllRC":
            case "Cumulative":
            case "Gravity":
                csvPaserRet = new CSVParser(csvPath,"Ret");
                break;
            case "Retrieval":
                csvPaserRes = new CSVParser(csvPath,"Res");
                break;
            case "Performance":
                csvPaserPer = new CSVParser(csvPath,"Per");
                break;
            case "ReadSts":
                csvPaserPer = new CSVParser(csvPath,"Per");
                csvPaserRes = new CSVParser(csvPath,"Res");
                csvPaserRet = new CSVParser(csvPath,"Ret");
        } // End Switch
    } // End Function fillAutoParameters

    private void readParamsFromFile(String paramFile) throws Exception
    {
        // Read All Parameters and check that all of them are true
        p = JAXB.unmarshal(new File(paramFile), ExperimentSetParams.class);
       fillAutoParameters();
    } // End Function

    private void printOutput (String outPath , String outText) throws Exception
    {
        PrintWriter pr = new PrintWriter(outPath);
        pr.write(outText);
        pr.close();
    }
    private void fillExperimentParameterFile(String fileName , String model , String indexName , String maxResults)
    {
        XMLTextParser parser = new XMLTextParser(fileName);
        parser.setTagValue("indexName",indexName);
        parser.setTagValue("maxResults",maxResults);
        parser.setTagValue("model",model);
        parser.save();
    } // End Function

    private String getCofficientCaption()
    {
        String result = "";
        if (p.model.equals("BM25"))
            result = "b";
        else if (p.model.equals("PL2"))
            result = "c";
        else if (p.model.equals("LMD"))
            result = "mu";
        return result;
    }

    private String getPerformanceFile (String coefficient)
    {
        // /Per/WA-BM25-BI-50-C1000-b0.1.trec
        String result = filePrefix.replace("ExperimentFolder","Per");
        result += getCofficientCaption() + coefficient + ".trec";
        return result;
    } // End Function

    private String getResFile (String coefficient)
    {
       String result = filePrefix.replace("ExperimentFolder","Res");
       result += getCofficientCaption() + coefficient + ".res";
       return result;
    } // End Function

    private String getRetFile (String b , String coefficient)
    {
        // /Ret/WA-PL2-FI-300K-C100-gb0-c5.ret

        String result = filePrefix.replace("ExperimentFolder","Ret");
        result =  String.format("%sgb%s-%s%s.ret",result,b,getCofficientCaption(), coefficient);
        return result;
    } // End Function

    private String runRetrievalStatistics (String b)
    {
        int maxResultsInt;
        StatisticsRetrieval sts = new StatisticsRetrieval();
        String outFileName = getResFile(b);
        maxResultsInt = Integer.parseInt(p.maxResults);
        sts.calculateStatistics(outFileName,maxResultsInt);
        return String.format("%d,%d,%d", sts.lineCtr , sts.docCtr , sts.limitedQryCtr);
    }
    private String runRCStatistics (String coefficient , String b)
    {
        StatisticsRetrievabilityCalculator sts = new StatisticsRetrievabilityCalculator();
        String outFileName = getRetFile(b,coefficient);
        sts.calculateStatistics(outFileName);
        return String.format("%1.6f,%d,%1.4f",sts.G , sts.zeroDocCtr , sts.sum );
    }

    private void runRetrievalExperiment(String coefficient)
    {
        // Run RetrievalApp Experiment for given B Value
        String outFileName , param , value ;
        FieldedRetrievalApp fRetApp;
        RetrievalApp retApp;

        XMLTextParser parser = new XMLTextParser(p.retrievalParamsFile);
        outFileName = getResFile(coefficient);
        param = getCofficientCaption();
        parser.setTagValue(param, coefficient );
        parser.setTagValue("resultFile",outFileName);
        parser.setTagValue("queryFile",p.queryFile);
        parser.setTagValue("indexName",p.indexName);
        parser.setTagValue("maxResults",p.maxResults);
        parser.setTagValue("model",p.model);
        parser.save();
        if (p.indexName.contains("Fielded"))
        {
            fRetApp = new FieldedRetrievalApp(p.retrievalParamsFile);
            fRetApp.processQueryFile();
        } // End IF
        else
        {
            retApp = new RetrievalApp(p.retrievalParamsFile);
            retApp.processQueryFile();
        } // End Else
        value = runRetrievalStatistics(coefficient);
        csvPaserRes.addCSVLineToMap(getResKey(coefficient),value);

    } // End Function

    private void runRCExperiment (String coefficient , String b)
    {
        // Run Retrievability Calculator Experiments for given B Value
        String outFileName , value , retKey;
        XMLTextParser parser = new XMLTextParser(p.retrievabilityParamsFile);
        outFileName = getResFile(coefficient);
        parser.setTagValue("resFile",outFileName);
        outFileName = getRetFile(b,coefficient);
        parser.setTagValue("retFile",outFileName);
        parser.setTagValue("indexName",p.indexName);
        parser.setTagValue("c",p.maxResults);
        parser.setTagValue("b",b);
        parser.save();
        RetrievabilityCalculatorApp rcApp = new RetrievabilityCalculatorApp(p.retrievabilityParamsFile);
        rcApp.setMap(initMap);
        rcApp.calculate();
        retKey = getRetKey(coefficient, b);
        value = String.format("%1.6f,%d,%1.4f",rcApp.G , rcApp.zeroRCtr, rcApp.rSum);
        csvPaserRet.addCSVLineToMap(retKey,value);
        // return runRCStatistics(coefficient,b);
    } // End Function
    private void runPerformanceExperiment(String coefficient) throws Exception
    {
        String inFile ,
                line,
                value = "",
                keys[] = {"map","bpref","P10"};
        BufferedReader br;
        int i = 0;
        inFile = getPerformanceFile(coefficient);
        br = new BufferedReader(new FileReader(inFile));

        while ((line = br.readLine()) != null && i < keys.length )
            if (line.startsWith(keys[i]))
            {
                value += "," + line.split("\t",3)[2];
                i++;
            } // End if
        value = value.replaceFirst(",","");
        csvPaserRes.addCSVLineToMap(getResKey(coefficient),value);
    } // End Function

    private boolean validRetrievability()
    {
        return p.maxResults.equals("100");
    }
    private String getResKey(String coefficient)
    {
        return defaultCSVKey + "," + coefficient ;
    }

    private String getRetKey(String coefficient , String b)
    {
        return String.format("%s,%s,%s" ,defaultCSVKey , b , coefficient);
    }

    private void runALLRC (String coefficient)
    {
        String  b;
        // Calculation
        if (validRetrievability())
        {
            b = "0";
            runRCExperiment(coefficient,b);
            b = "0.5";
            runRCExperiment(coefficient,b);
        } // End if (validRetrievability())
}

private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss");
        return LocalDateTime.now().format(dtf);
    }

    private String[] getCoefficientRange()
    {
        /*
        Get Coefficient Values based on input model
         */
        String[]
                 BM25Set = {"0.1", "0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1"},
                 PL2Set = {"0.1", "0.5", "1", "5", "10", "15", "20", "50"},
                 LMDSet = {"100","200","300","400","500","600","700","800","900","1000","5000"},
               //  BM25Set = {"0.1", "0.3","0.5","0.7","0.9"},
               //  PL2Set = {"0.1", "1", "10", "20", "50"},
               //  LMDSet = {"100","200","300","400","500","600","700","800"},

                // Complement
                 result;
        if (p.model.equals("BM25"))
            result = BM25Set;
        else if (p.model.equals("PL2"))
            result = PL2Set;
        else result = LMDSet;
        return result;
    }

    private void processExperimentSet () throws Exception
    {
        // This function is used to process the whole experiment Set
         String coefficient , b , bashLines = "" , value , retKey  ;
         String coefficientRange[] = getCoefficientRange();
         boolean updateRes = false , updateRet = false , updatePer = false;
         for (int i = 0; i < coefficientRange.length; i++) {
             coefficient = coefficientRange[i];

        switch (p.exType)
            {
                case "All":
                    runRetrievalExperiment(coefficient);
                    // Checking ValidRetrievability Inside These Functions
                    runALLRC(coefficient);
                    bashLines += getBashLine(coefficient);
                    updateRes = true;
                    updateRet = true;
                    break;
                case "AllRC":
                    runALLRC(coefficient);
                    updateRet = true;
                    break;
                case "Cumulative":
                    if (validRetrievability())
                        runRCExperiment(coefficient,"0");
                    updateRet = true;
                    break;
                case "Gravity":
                    if (validRetrievability())
                        runRCExperiment(coefficient,"0.5");
                    updateRet = true;
                    break;

                case "Retrieval":
                    runRetrievalExperiment(coefficient);
                    bashLines += getBashLine(coefficient);
                    updateRes = true;
                    break;
                case "Performance" :
                    if (!validRetrievability())
                        runPerformanceExperiment(coefficient);
                    updatePer = true;
                    break;
                case "ReadSts":
                    if (validRetrievability())
                        {
                        value = runRetrievalStatistics(coefficient);
                        csvPaserRes.addCSVLineToMap(getResKey(coefficient),value);
                        updateRes = true;
                        b = "0";
                        retKey = getRetKey(coefficient,b);
                        value = runRCStatistics(coefficient,b);
                        csvPaserRet.addCSVLineToMap(retKey,value);
                        b = "0.5";
                        retKey = getRetKey(coefficient,b);
                        value = runRCStatistics(coefficient,b);
                        csvPaserRet.addCSVLineToMap(retKey,value);
                        updateRet = true;
                      } // End if
                   else
                    {
                        runPerformanceExperiment(coefficient);
                        updatePer = true;
                    } // End Else
            } // End Switch
        } // End For

        if (!bashLines.isEmpty())
            printOutput(filePrefix.replace("ExperimentFolder","Bash") + "-bash.sh"
                      , bashLines);
        if (updateRes)
            csvPaserRes.updateCSVFile();
        if (updateRet)
            csvPaserRet.updateCSVFile();
        if (updatePer)
            csvPaserPer.updateCSVFile();
    } // End Function

    private void runExperimentFile (String fileName)
    {
        try {
            System.out.println("Experiment " + fileName + " started at : " + getCurrentTime());
            readParamsFromFile(fileName);
            processExperimentSet();
            System.out.println("Experiment " + fileName + " is done successfully at : " + getCurrentTime());
         } // End Try
            catch (Exception e) {
            e.printStackTrace();
        } // End Catch
    } // End Function

    private void runFileList (String folderName) {
        CrossDirectoryClass cr = new CrossDirectoryClass();
        ArrayList<String> fileList = cr.crossDirectory(folderName, false);
        RunExperimentSet re = new RunExperimentSet();
        for (String file : fileList)
            runExperimentFile(file);
    }

    public void runCalculatedList ()
    {
        String paramFileName =  "params/BMExperimentSets/Experiment2.xml";
       String[] indexNames = {
              /* "AquaintBigramIndex","AquaintCombinedIndex","AquaintUnigramIndex" , "AquaintFieldedIndex",
               "Core17UnigramIndex","Core17BigramIndex","Core17CombinedIndex"  , "Core17FieldedIndex"*/
             //  "WAPOUnigramIndex","WAPOBigramIndex","WAPOCombinedIndex"  , "WAPOFieldedIndex"
               "WAPOUnigramIndex"
       };
       String maxResults[] = {"100","1000"};
      // String models[] = {"BM25","PL2" , "LMD"};
       String models[] = {"BM25"};
       for (int m = 0 ; m < models.length ; m++)
           for ( int i = 0 ; i < indexNames.length ; i++)
               for (int j=0 ; j < maxResults.length ; j++ )
               {
                   fillExperimentParameterFile(paramFileName , models[m] , indexNames[i] , maxResults[j]);
                   runExperimentFile(paramFileName);
               } // End For J
       // runExperimentFile(paramFileName);
    }

    private void cloneMap()
    {
        RetrievabilityCalculatorApp retApp = new RetrievabilityCalculatorApp("");
        String indexName =  "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments\\Indexes\\WAPOUnigramIndex";
        try {

            initMap = retApp.cloneMap(indexName);
            retApp = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args)
        {
        // write your code here
        String sourceFile = "params\\BMExperimentSets\\Experiment2.xml";

        RunExperimentSet re = new RunExperimentSet();
        re.runCalculatedList();
        // re.runExperimentFile(sourceFile);

    } // End Function Main
} // End Class

class ExperimentSetParams  {
    public String indexName,
            maxResults,
            queryFile,
            exType,
            model,
            retrievalParamsFile,
            retrievabilityParamsFile,
            csvPath,
            mainDir;
}
