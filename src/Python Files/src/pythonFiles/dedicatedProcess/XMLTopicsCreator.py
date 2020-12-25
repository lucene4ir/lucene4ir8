# <top>
# <num> Number: 321 </num>
# <title>
# Women in Parliaments
# </title>
# <desc> Description:
# Pertinent documents will reflect the fact that women continue to be poorly represented in parliaments across the world, and the gap in political power between the sexes is very wide, particularly in the Third World.
# </desc>
# <narr> Narrative
# Pertinent documents relating to this issue will discuss the lack of representation by women, the countries that mandate the inclusion of a certain percentage of women in their legislatures, decreases if any in female representation in legislatures, and those countries in which there is no representation of women.
# </narr>
# </top>

def generateXMLTopics(sourceFile , destFile):

    f1 = open(sourceFile,'r')
    f2 = open(destFile,'w')
    for line in f1:
        parts = line.split(' ' , 1)
        num = parts[0]
        qry = parts[1]
        newLine = ("<top>\n" + \
                  "<num> Number: %s </num>\n" + \
                  "<title> \n%s</title>\n" + \
                  "<desc> \n \n</desc>\n<narr> \n \n</narr>\n</top>\n\n") % (num,qry)
        #"<desc> Description:\nDesc\n</desc>\n<narr>Narrative:\nNa\n</narr>\n</top>\n\n") % (num, qry)
        f2.write(newLine)
    f1.close()
    f2.close()

def generateCommands(b):
    if (b < 10):
        b = "0." + str(b)
    else:
        b = "1.0"
    command = "nohup target/appassembler/bin/SearchCollection -index lucene-index.core18.pos+docvectors+rawdocs \ \n" + \
              "-topicreader Trec -topics data/WAPO/300KXML.qry \ \n" + \
              "-bm25 -b %s -k1 1.2 -rm3 -rerankCutoff 100 -output out/result%s.res &\n" % (b,b)
    return command
def generateBash():
    path = r"C:\Users\kkb19103\Desktop\bash.sh"
    f = open(path, 'w')
    for i  in range(10):
        line = generateCommands(i+1)
        f.write(line)
    f.close()

if __name__ == '__main__':
    path = r"C:\Users\kkb19103\Desktop"
    sourceFile = path + "\\300K.qry"
    destFile = path + "\\300KXML.qry"
    generateXMLTopics(sourceFile,destFile)
    #generateBash()