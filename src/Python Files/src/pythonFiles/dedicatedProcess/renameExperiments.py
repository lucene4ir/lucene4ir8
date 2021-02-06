import os

def renameFiles (dirName,oldPart,newPart):
    folder = os.listdir(dirName)
    dirName += '\\'
    for fileName in folder:
        num = fileName.replace('XML','').replace('.xml','')
        if (len(num) < 6):
            newFile = "{:06d}".format(int(num))
            newFile = "XML%s.xml" % newFile
            os.rename(dirName + fileName, dirName + newFile)
        # if (fileName.find(oldPart) >= 0):
        #     newFile = fileName.replace(oldPart,newPart)
        #     print(fileName , newFile)
        #     os.rename(dirName+fileName,dirName+newFile)
def main():
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\revertedIndex\XML'
    oldPart = 's5-'
    newPart = 's05-'
    renameFiles(path,oldPart,newPart)

if __name__ == '__main__':
    main()