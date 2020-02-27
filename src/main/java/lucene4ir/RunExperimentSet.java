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
            defaultCSVKey,filePrefix;
    private CSVParser csvPaser;
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
        String qryCount = "" , indexFolder = "" , C = "", corpus , csvFile ,
        outPutFolder = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments\\WAPO 26-02-2020";

        if (validRetrievability() &&
                initMap == null &&
                p.exType.contains("All")
        )
            cloneMap();
        C = "C" + p.maxResults;
        if (validRetrievability())
      //  if (p.maxResults.equals("100"))
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
        //   out\Core17\UnigramIndex\50\C1000\CombinedgramFilter
        filePrefix = String.format("%s\\%s-%s-%s-%s-%s-" ,
                outPutFolder , corpus.substring(0,2) ,
                p.model , indexFolder.substring(0 ,1) + "I"
                , qryCount , C);

        if (p.csvFile.isEmpty())
            csvFile = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments\\out.csv";
        else
            csvFile = p.csvFile;
        csvPaser = new CSVParser();
        csvPaser.readFromFile(csvFile);

        /*CSV Key
        corpus - indexType - qryFilter - qryCount - model - maxResults - RetrievabilityB - RetrievalCoefficient (B , mu , c) */
        defaultCSVKey = String.format("%s,%s,%s,%s,%s,%s,",
                corpus,indexFolder , "combinedQuery" , qryCount , p.model , p.maxResults);
        p.indexName = outPutFolder + "\\Indexes\\" + p.indexName;
    }
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

    private String getResFile (String coefficient)
    {
       return filePrefix + "result" + coefficient + ".res";
    }

    private String getRetFile (String b , String coefficient)
    {
        // WA-BM25-BI-300K-C100-0.5-RCResults0.1.ret
        return String.format("%s%s-RCResults%s.ret",filePrefix,b,coefficient);
    }

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

    private String runRetrievalExperiment(String b)
    {
        // Run RetrievalApp Experiment for given B Value
        String outFileName , param;
        FieldedRetrievalApp fRetApp;
        RetrievalApp retApp;

        XMLTextParser parser = new XMLTextParser(p.retrievalParamsFile);
        outFileName = filePrefix + "result" + b + ".res";
        if (p.model.equals("BM25"))
            param = "b";
        else if (p.model.equals("PL2"))
            param = "c";
        else
            param = "mu";
        parser.setTagValue(param, b );
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
        return runRetrievalStatistics(b);
    } // End Function

    private String runRCExperiment (String coefficient , String b)
    {
        // Run Retrievability Calculator Experiments for given B Value
        String outFileName ;
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
        return String.format("%1.6f,%d,%1.4f",rcApp.G , rcApp.zeroRCtr, rcApp.rSum);
        // return runRCStatistics(coefficient,b);
    } // End Function
    private String getPerformanceValues(String b) throws Exception
    {
        String inFile ,
                line,
                result = "",
                keys[] = {"map","bpref","P10"};
        BufferedReader br;
        int i = 0;
        inFile = filePrefix + "trec" + b + ".trec";
        br = new BufferedReader(new FileReader(inFile));

        while ((line = br.readLine()) != null && i < keys.length )
            if (line.startsWith(keys[i]))
            {
                result += "," + line.split("\t",3)[2];
                i++;
            } // End if
        result = result.replaceFirst(",","");
        return result;
    } // End Function

    private String getNewKey (String retB , String b)
    {
        return defaultCSVKey + retB + "," + b;
    }

    private boolean validRetrievability()
    {
        return p.maxResults.equals("100");
    }
    private void runALLRC (String coefficient)
    {
        String result , b;
        // Calculation
        if (validRetrievability())
        {
            b = "0";
            result = runRCExperiment(coefficient,b);
            // *** Statistics ***
            csvPaser.setRet(result);
            csvPaser.addCSVLineToMap();

            b = "0.5";
            result = runRCExperiment(coefficient,b);
            csvPaser.setKey(getNewKey(b,coefficient));
            csvPaser.setRet(result);
        } // End if (validRetrievability())
}

private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss");
        return LocalDateTime.now().format(dtf);
    }

    private String[] getbRange()
    {
        /*
        Get Coefficient Values based on input model
         */
        String[]
                // BM25Set = {"0.1", "0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1"},
               //  PL2Set = {"0.1", "0.5", "1", "5", "10", "15", "20", "50"},
                 LMDSet = {"100","200","300","400","500","600","700","800","900","1000","5000"},
                 BM25Set = {"0.1", "0.3","0.5","0.7","0.9"},
                 PL2Set = {"0.1", "1", "10", "20", "50"},
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
         String coefficient , retB  = "0", bashLines = ""  ;
         String bRange[] = getbRange();
         for (int i = 0; i < bRange.length; i++) {
             coefficient = bRange[i];
         if (p.exType.equals("Gravity"))
             retB = "0.5";
        csvPaser.newLine(getNewKey(retB,coefficient));

        switch (p.exType)
            {
                case "All":
                    csvPaser.setRes(runRetrievalExperiment(coefficient));
                    // Checking ValidRetrievability Inside These Functions
                    runALLRC(coefficient);
                    bashLines += getBashLine(coefficient);
                    break;
                case "AllRC":
                    runALLRC(coefficient);
                    break;
                case "Cumulative":
                case "Gravity":
                    if (validRetrievability())
                        csvPaser.setRet(runRCExperiment(coefficient,retB));
                    break;

                case "Retrieval":
                    csvPaser.setRes(runRetrievalExperiment(coefficient));
                    bashLines += getBashLine(coefficient);
                    break;
                case "Performance" :
                    if (!validRetrievability())
                        csvPaser.setPerformance(getPerformanceValues(coefficient));
                    break;
                case "ReadSts":
                    if (validRetrievability())
                        {
                        retB = "0";
                        csvPaser.setKey(getNewKey(retB,coefficient));
                        csvPaser.setRes(runRetrievalStatistics(coefficient));
                        csvPaser.setRet(runRCStatistics(coefficient,retB));
                        csvPaser.addCSVLineToMap();
                        retB = "0.5";
                        csvPaser.setKey(getNewKey(retB,coefficient));
                        csvPaser.setRes(runRetrievalStatistics(coefficient));
                        csvPaser.setRet(runRCStatistics(coefficient,retB));
                      } // End if
                   else
                    {
                        retB = "0";
                        csvPaser.setKey(getNewKey(retB,coefficient));
                        csvPaser.setRes(runRetrievalStatistics(coefficient));
                        csvPaser.setPerformance(getPerformanceValues(coefficient));
                    } // End Else
            } // End Switch
            csvPaser.addCSVLineToMap();

        } // End For

        if (!bashLines.isEmpty())
            printOutput(filePrefix + "bash.sh" , bashLines);
        csvPaser.updateCSVFile();

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
               "WAPOFieldedIndex"
       };
       String maxResults[] = {"100","1000"};
       String models[] = {"BM25","PL2"};
      //   String models[] = {"LMD"};
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
            String sourceFile = "params\\BMExperimentSets\\Experiment2.xml",
                    result;
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
            csvFile;
}
