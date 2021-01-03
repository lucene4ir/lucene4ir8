import os

def renameFiles (dirName,oldPart,newPart):
    folder = os.listdir(dirName)
    dirName += '\\'
    for fileName in folder:
        if (fileName.find(oldPart) >= 0):
            newFile = fileName.replace(oldPart,newPart)
            print(fileName , newFile)
            os.rename(dirName+fileName,dirName+newFile)
def main():
    path = r'D:\Backup 16-12-2020\2nd Experiment - RM3\AllRes\Bias Measurement'
    oldPart = 's5-'
    newPart = 's05-'
    renameFiles(path,oldPart,newPart)

if __name__ == '__main__':
    main()