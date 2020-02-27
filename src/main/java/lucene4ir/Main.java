package lucene4ir;

import lucene4ir.Stats.StatisticsRetrieval;
import lucene4ir.utils.TokenAnalyzerMaker;
import lucene4ir.utils.XMLTextParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Main {
    private void testParser () throws IOException
    {
        String fName = "params\\retrieval_params.xml", bashLine , outputPath , model,
                outDirectory = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\Applications\\Trec_Eval\\trec_eval.8.1 Original\\",
                bashLineFormat = "./trec_eval ./Qrels/Aquaint-AnseriniQrel.qrel  ./Results/%s/AquaintUnigramIndex50-1000-C%1.1f.res > ./Results/%s/AquaintUnigramIndex50-1000-C%1.1f.trec\n",
                outputPathFormat = outDirectory + "Results\\AquaintUnigramIndex50-1000-C%1.1f.res",
                bashOutFile = outDirectory + "bash.sh";


        XMLTextParser parser = new XMLTextParser(fName);
        PrintWriter pr = new PrintWriter(bashOutFile);
        for (double c = 0.5 ; c <= 10 ; c+=0.5)
        {
           /* parser.setTagValue("c", String.valueOf(c) );
            outputPath = String.format(outputPathFormat,c);
            parser.setTagValue("resultFile",outputPath);
            parser.save();
            RetrievalApp re = new RetrievalApp(fName);
            re.processQueryFile();*/
            model = "BasicModelIN";
            bashLine = String.format(bashLineFormat,model,c,model, c);
            pr.write(bashLine);
        }
        pr.close();
    }


    private void testLog ()
    {
        int N = 1 , n = 1;

       double ans = (N-n+0.5) / (n+0.5);
       ans = Math.log(1+ ans);
    }

    private void analyzeThis (String input, String filter)  throws Exception
    {
        String term;
        TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
        Analyzer an = tam.createAnalyzer(filter);
        TokenStream ts = an.tokenStream("",input);
        CharTermAttribute ta =  ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken())
        {
            term = ta.toString();
            System.out.println(term);
        }
       tam = null;
        an.close();
        ts.close();
        ta = null;

    }
    public static void main(String[] args)  {
	RunExperimentSet re = new RunExperimentSet();
    re.runCalculatedList();

    }
}
