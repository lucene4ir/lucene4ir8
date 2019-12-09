/*
    This Class is Used to index CommonCore 2017 data files
   TREC - CommonCore 2017 Files
    URL : https://trec.nist.gov/data/core2017.html

   * Created by Abdulaziz on 16/03/2019.
   * Edited by Abdulaziz AlQattan on 28/06/2019.
*/

package lucene4ir.indexer;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.document.*;
import org.jsoup.safety.Whitelist;

import java.io.BufferedReader;

public class CommonCoreDocumentIndexer extends DocumentIndexer {

    // Properties

    Whitelist whiteList;
    private org.jsoup.nodes.Document jdoc;
    private Field fldDocID ,
                  fldPubDate ,
                  fldTitle,
                  fldContent,
                  fldAll,
                  fldBiAll;
    private Document doc;

    // Sub- Private Functions
    private void initWhiteList()
    {
        // Add White List Of The Document
        // The White List Are The Tags to keep in the Document after Reading From Jsoup
        try {
            // Add The popular tags in the white list
            whiteList = Whitelist.relaxed();

            // Add each allowed tag with its allowed attributes
            whiteList.addTags("title");

            // *******************

            whiteList.addTags("doc-id");
            whiteList.addAttributes("doc-id","id-string");

            // *******************

            whiteList.addTags("doc.copyright");
            whiteList.addAttributes("doc.copyright","year");
            whiteList.addAttributes("doc.copyright","holder");

            // *******************

            whiteList.addTags("classifier");

            // *******************

            whiteList.addTags("pubdata");
            whiteList.addAttributes("pubdata","name");
            whiteList.addAttributes("pubdata","date.publication");

            whiteList.addTags("hl1");

        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    } // End Function
    private String getFieldByAttribute(String TagName  , String AttributeName )
    {
        /*
        This Function is used to get the attribure value of a specific tag
        in a specific jsoup Document
         */
        return  jdoc.getElementsByTag(TagName).attr(AttributeName);
    } // End Function
    private String getFieldByTag(String TagName){
           /*
           This function is used to main gathered text from  all elements with a specific tag
           in the input jsoup document
            */
        String fieldText = "";
        org.jsoup.select.Elements dns = jdoc.getElementsByTag(TagName);
        if (dns.size() > 0)
            fieldText = dns.text();

        return fieldText;
    } // End Function

    private void addFieldWithValue (Field fld , String val )
    {
        // This Function is used to set the given value to the given field Then add the field to the private current Document
       fld.setStringValue(val);
        doc.add(fld);
    } // End Function

    private String getPubDate()
    {
        /*
        This function is Used to get the Publication Date :
         */
        String result = "" ;
        result = getFieldByAttribute( "pubdata" , "date.publication");
        if (!result.isEmpty())
            result = result.substring(0,4) + "/" + result.substring(4,6) + "/" + result.substring(6,8);
        return result;
    } // End Function


  /*  public void setAnalyzer(boolean fielded, String defaultTokenFilterFile)
    {
        String biFldName = "biraw";
        String biTokenFilterFile = "params/index/TokenFilterFile_Bigram.xml";
        String uniTokenFilterFile = "params/index/TokenFilterFile_Unigram.xml";
        TokenAnalyzerMaker tam = new TokenAnalyzerMaker();

        if (fielded)
        {
            Analyzer defaultAnalyzer = tam.createAnalyzer(uniTokenFilterFile);
            HashMap<String,Analyzer> analyzerPerField = new HashMap<String,Analyzer>();
            Analyzer biAnalyzer = tam.createAnalyzer(biTokenFilterFile);
            analyzerPerField.put(biFldName, biAnalyzer);
            PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(defaultAnalyzer , analyzerPerField);
            analyzer = wrapper;
            if(indexPositions)
                fldBiAll = new lucene4ir.indexer.TermVectorEnabledTextField(biFldName, "", Field.Store.YES);
            else
                fldBiAll = new TextField(biFldName, "", Field.Store.YES );
        }
        else
            analyzer = tam.createAnalyzer(defaultTokenFilterFile);
    } // End Function*/


    private void initFields()
    {
        /*
        Initialize Fields Collection in the Following Sequence :
        1= Document Number - String Field
        2- Publish Date - String Field
        3- Title - Text Field
        4- Content - Text Field
        5- All - Text Field
 */
        doc = new Document();
        // String Fields
        fldDocID = new StringField(Lucene4IRConstants.FIELD_DOCNUM, "", Field.Store.YES);
        fldPubDate = new StringField(Lucene4IRConstants.FIELD_PUBDATE, "", Field.Store.YES);
        // Indexed Fields
        if (indexPositions)
        {
            fldTitle = new lucene4ir.indexer.TermVectorEnabledTextField(Lucene4IRConstants.FIELD_TITLE, "",  Field.Store.YES);
            fldContent = new lucene4ir.indexer.TermVectorEnabledTextField(Lucene4IRConstants.FIELD_BODY, "", Field.Store.YES);
            fldAll = new lucene4ir.indexer.TermVectorEnabledTextField(Lucene4IRConstants.FIELD_RAW, "", Field.Store.YES);
            if (fielded())
                fldBiAll = new lucene4ir.indexer.TermVectorEnabledTextField(tokenizedFields.getFieldName(0), "", Field.Store.YES);
        } // End if
        else
        {
            fldTitle = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            fldContent = new TextField(Lucene4IRConstants.FIELD_BODY, "", Field.Store.YES);
            fldAll = new TextField(Lucene4IRConstants.FIELD_RAW, "", Field.Store.YES);
            if (fielded())
                fldBiAll = new TextField(tokenizedFields.getFieldName(0), "", Field.Store.YES);
        } // End Else


    } // End Function

    // Constructor Method
    public CommonCoreDocumentIndexer(String indexPath, String tokenFilterFile ,  boolean positional){

       super(indexPath, tokenFilterFile, positional);

        // Create String Corpus Fields and add them to the Document doc
        initFields();
        initWhiteList();
    } // End Function
    public void finished  ()
    {
        // This function is used to release the memory of the current instance
        super.finished();
        doc.clear();
        fldDocID =  null;
        fldPubDate = null;
        fldTitle = null;
        fldAll = null;
        fldBiAll = null;
        fldContent = null;
    } // End Function

    public void indexDocumentsFromFile(String filename){
        /*
        This method is used to  :
            1- read an input file (filepath) Line by Line
            2- Gather all of these lines in one Text String
            3- add a Line Separator between Lines in the Resultant Text
            4- Send The Resultant Text To The Method extractFieldsFromXmlAndIndex
                to identify the xml tags and Index them according to the needs
         */
        String line , XMLText = "";
        short lineNumber = 1;
        try {
            BufferedReader br = openDocumentFile(filename);
            try {
                line = br.readLine();
                while (line != null){
                    if (lineNumber > 3 && !line.startsWith("</nitf>"))
                        XMLText += line + System.lineSeparator();
                    line = br.readLine();
                    lineNumber++;
                }
            } finally {
                if (!XMLText.isEmpty())
                    extractFieldsFromXmlAndIndex(XMLText);
                br.close();
            }
        }
        catch (Exception e){
            System.out.println(" caught a " + e.getClass() +  e.getLocalizedMessage() +
                    "\n with message: " + e.getMessage());
            System.exit(0);
        }
    } // End Function

    public void extractFieldsFromXmlAndIndex(String xmlString){
            String   docid,
                     title ,
                     content,
                     source="Unknown",
                     pubdate ="",
                     safeText = org.jsoup.Jsoup.clean(xmlString,whiteList),
                    all;

            jdoc = org.jsoup.Jsoup.parse(safeText);
            // Get Document NUmber
            docid = getFieldByAttribute("doc-id" , "id-string").trim();
            // Get Title
            title = getFieldByTag("title");

            // Get PubDate
            pubdate = getPubDate();
            // Get Source (Publisher)
            source = getFieldByAttribute( "pubdata" , "name");

            // Get Contents of the File
            content = pubdate;
            content += getFieldByAttribute("doc.copyright","year" ) + " ";
            content += getFieldByAttribute("doc.copyright","holder") + " ";
            content += getFieldByTag( "classifier") + " ";
            content += source + " ";
            content  += getFieldByTag( "hl1") + " ";
            content += getFieldByTag( "p") + " ";

            // Gather All in All (RAW) Field
            all = title + " " + content + " " + source + " " + pubdate;
            all = getFieldByTag( "p");
           // Clear The Document
           doc.clear();
            // Move from Values Array to Fields Array and add to a new Document to Preparre it
            // Check Whether to add biGram RAW Field
            addFieldWithValue(fldDocID,docid);
            addFieldWithValue(fldPubDate,pubdate);
            addFieldWithValue(fldTitle,title);
            addFieldWithValue(fldContent,content);
            addFieldWithValue(fldAll,all);

        if (fielded())
                addFieldWithValue(fldBiAll,all);
           System.out.println(String.format("Adding document: %s Title %s" , docid , title));
           // Add the resultant document to the Indexer
           addDocumentToIndex(doc);
        } // End Function
} // End Class




