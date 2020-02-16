/**
 *
 * This Class is Used to index CommonCore 2018 (WAPO) data files
 *    INDEXERS - GITHUB REFERENCE PAGE
 *     URL : https://github.com/ABDULAZIZALQATAN/Indexers
 * Created by Abdulaziz on 10/12/2019.
 * Edited by Abdulaziz on 11/12/2019.
 */

package lucene4ir.indexer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lucene4ir.Lucene4IRConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.sql.Date;
import java.text.SimpleDateFormat;


public class WapoDocumentIndexer extends DocumentIndexer {
    // Properties
    private Field fldDocID ,
            fldPubDate ,
            fldTitle,
            fldContent,
            fldAuthor,
            fldFullCaption,
            fldArticle_URL,
            fldKicker,
            fldAll,
            fldBiAll;
    String all;
    int lineNum;
    private Document doc;

    private void initFields()
    {
        /*
        Initialize Fields Collection in the Following Sequence :
        1- DocID
        2- Title
        3- Author
        4- Publish Date
        5- Contents
        6- Full_Caption
        7- Article_URL
        8- Kicker
        9- All
 */
        doc = new Document();
        // Short String Fields (Single Word Fields )
        fldDocID = new StringField(Lucene4IRConstants.FIELD_DOCNUM, "", Field.Store.YES);
        fldPubDate = new StringField(Lucene4IRConstants.FIELD_PUBDATE, "", Field.Store.YES);
        fldKicker = new StringField(Lucene4IRConstants.FIELD_KICKER, "", Field.Store.YES);
        fldArticle_URL = new StringField(Lucene4IRConstants.FIELD_ARTICLE_URL, "", Field.Store.YES);

        // Term Vector Fields (More than one word)
        if (indexPositions)
        {
            fldTitle = new lucene4ir.indexer.TermVectorEnabledTextField(Lucene4IRConstants.FIELD_TITLE, "",  Field.Store.YES);
            fldFullCaption = new lucene4ir.indexer.TermVectorEnabledTextField(Lucene4IRConstants.FIELD_FULLCAPTION, "",  Field.Store.YES);
            fldAuthor = new lucene4ir.indexer.TermVectorEnabledTextField(Lucene4IRConstants.FIELD_AUTHOR, "",  Field.Store.YES);
            fldContent = new lucene4ir.indexer.TermVectorEnabledTextField(Lucene4IRConstants.FIELD_BODY, "", Field.Store.YES);
            fldAll = new lucene4ir.indexer.TermVectorEnabledTextField(Lucene4IRConstants.FIELD_RAW, "", Field.Store.YES);
            if (fielded())
                fldBiAll = new lucene4ir.indexer.TermVectorEnabledTextField(tokenizedFields.getFieldName(0), "", Field.Store.YES);
        } // End if
        else
        {
            fldTitle = new TextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            fldFullCaption = new TextField(Lucene4IRConstants.FIELD_FULLCAPTION, "",  Field.Store.YES);
            fldAuthor = new TextField(Lucene4IRConstants.FIELD_AUTHOR, "",  Field.Store.YES);
            fldContent = new TextField(Lucene4IRConstants.FIELD_BODY, "", Field.Store.YES);
            fldAll = new TextField(Lucene4IRConstants.FIELD_RAW, "", Field.Store.YES);
            if (fielded())
                fldBiAll = new TextField (tokenizedFields.getFieldName(0), "", Field.Store.YES);
        } // End Else

    }

    // Constructor Method (Initialization)
    public WapoDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional){
        // Constructor Method to initialize the Fields Collection
        super(indexPath, tokenFilterFile, positional);
        initFields();
    }

    public void finished  ()
    {
        // This function is used to release the memory of the current instance
        super.finished();
        doc.clear();
        // String Fields (One Word)
        fldDocID =  null;
        fldPubDate = null;
        fldKicker = null;
        fldArticle_URL = null;
        // Term Vector Fields
        fldTitle = null;
        fldAll = null;
        fldBiAll = null;
        fldContent = null;
        fldFullCaption = null;
        fldAuthor = null;
    } // End Function


    private String getJsonElementValue (JsonElement jElement , String elementName)
    {
        String result = "";
        if (jElement.isJsonObject())
           result = jElement.getAsJsonObject().get(elementName).toString().trim().replaceAll("\"", "");
        return result;
    }
    private String setFieldValue (Field fld , JsonElement jElement , String elementName)
    {
           /*
           This Function is Used to
           1- Extract The Value from input Json Element By its Name
           2- Set The Value to the Given Field
           3- Add The Given Field to the Current Document
           4- Add the value to the public All Variable
           */
        String value = "";
        value = getJsonElementValue(jElement,elementName);
        if (!value.isEmpty())
        {
            if (elementName.equals("published_date"))
                value = getPubDate(value);
            all += value + " ";
            fld.setStringValue(value);
            doc.add(fld);
        } // End if
        return value;
    } // End Function

    private String getPubDate (String inDate)
    {
         // Given date in Milli Seconds - return String Date formatted by ( dd/mm/yyyy )

        String result = "";

         if (StringUtils.isNumeric(inDate.trim()))
         {
             Date aDate = new Date(Long.parseLong(inDate));
             SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
             result = df.format(aDate);
         }
        return result;
    } // end Function

   public void parseLine(String line)
    {
         /*
        This Function is used to  :
        1- Parse a Single Json Line From Washingtop POST (WAPO)  Corpus Document
        2- Extract the required properties from the Line
        3- Add The Properties to Corpus Fields
        4- Insert The corpusFields Collection to the Corpus Document
        5- Index The Resultant Document

        The Fields To Index in Each Lines are :
            1- DocID
            2- Title
            3- Author
            4- Publish Date
            5- Contents
            6- Full_Caption
            7- Article_URL
            8- Kicker
            9- All
        */
        // Local Variables
        JsonParser jParser;
        JsonElement jElement ;
        boolean captionAdded , kickerAdded;
        String contentValue , jElementInfo,docid , title,
                elementNameID = "id",
                elementNameTitle = "title",
                elementNameAuthor = "author",
                elementNamePubDate = "published_date",
                elementNameKicker = "kicker",
                elementNameFullCaption = "fullcaption",
                elementNameContent = "contents",
                elementNameArticleURL = "article_url",
                elementTypeContent = "sanitized_html";

        // Parse The input Line
        jParser = new JsonParser();
        jElement = jParser.parse(line);
        JsonArray jContentElements;
        // Check If the Line is a Valid Json Object
        if (jElement.isJsonObject()) {
            // Add Values to Fields
            // Use Field Names identical to json File Field Names that might not be similar to our field names

            // Set child-Root Node Fields ( Directly Under Root )
            // DocID Field
            all = "";
            doc.clear();
            docid = setFieldValue(fldDocID,jElement,elementNameID);

            // Title Field
            title = setFieldValue(fldTitle,jElement,elementNameTitle);
            // Author Field
            setFieldValue(fldAuthor,jElement,elementNameAuthor);
            // Publish Date Field
            setFieldValue(fldPubDate,jElement,elementNamePubDate);

            // Article_URL Field
            setFieldValue(fldArticle_URL,jElement,elementNameArticleURL);

            // Get Values From Child-Contents Nodes ( Nodes Under Contents Node )
            jContentElements = jElement.getAsJsonObject().get(elementNameContent).getAsJsonArray();
            // Change Contents Group Name Back to Single Content Name
            elementNameContent = "content";

            contentValue = "";
            captionAdded = false;
            kickerAdded = false;
            for (JsonElement element : jContentElements) {
                if (element.isJsonObject())
                {
                    jElementInfo = getJsonElementValue(element,"type");
                    // Add Content Field
                    if (jElementInfo.equals(elementTypeContent))
                        contentValue += getJsonElementValue(element,elementNameContent)
                                + " ";
                    else if (element.getAsJsonObject().has(elementNameFullCaption))
                    // Add Full Caption Field
                    {
                        setFieldValue(fldFullCaption,element,elementNameFullCaption);
                        captionAdded = true;
                    } // End if (element.getAsJsonObject().has(elementNameFullCaption))
                    // Add Kicker Field
                    else if (element.getAsJsonObject().has(elementNameKicker))
                    {
                        setFieldValue(fldKicker,element,elementNameKicker);
                        kickerAdded = true;
                    }

                } // End if (element.isJsonObject())
            }; // End For
            if (!captionAdded)
            {
                fldFullCaption.setStringValue("");
                doc.add(fldFullCaption);
            }
            if (!kickerAdded)
            {
                fldKicker.setStringValue("");
                doc.add(fldKicker);
            }
            contentValue = Jsoup.parse(contentValue.trim()).text();
            fldContent.setStringValue(contentValue);
            doc.add(fldContent);
            all += contentValue;
            all = all.trim();
            fldAll.setStringValue(all);
            doc.add(fldAll);
            if (fielded())
                {
                    fldBiAll.setStringValue(all);
                    doc.add(fldBiAll);
                } // End if
            System.out.println(String.format("Adding document:  Number:%d - ID : %s  - Title %s" ,
                                             ++lineNum , docid , title));
            addDocumentToIndex(doc);
        } // End if (jElement.isJsonObject())
    } // End Function
        public void indexDocumentsFromFile (String fileName){
        /*
        This method is used to Read The Input Corpus File and Line by Line than
        Parse each document in each line
         */
            // Local Variables for a Single Line and Line Separator
            String line;
            // Read The Input File into a Buffer
            try {
                BufferedReader br = openDocumentFile(fileName);
                // Iterate Through Lines and Parse Lines each by each
                lineNum = 0;
                while ((line = br.readLine()) != null)
                  //  if (lineNum++ >= 386087 )
                        parseLine(line); // Send The Current Line To The Parse Line Function
            }  // End Try
            catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            } // End CATCH
        } // End Function
}
