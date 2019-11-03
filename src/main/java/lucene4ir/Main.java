package lucene4ir;

import lucene4ir.utils.XMLTextParser;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
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
}
