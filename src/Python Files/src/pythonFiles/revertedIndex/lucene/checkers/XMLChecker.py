import os.path as pth

def main():
    folder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\revertedIndex\XML\XML#num.xml'
    # files =  os.listdir(folder)
    # path = folder + '\XML1.xml'
    ctr = 0
    for i in range(1 , 123104):
        path = folder.replace('#num' , str(i))
        if not pth.exists(path):
            print (i , ' is not exist')
            ctr += 1
    print('Missing' , ctr , 'queries')
if __name__ == '__main__':
    main()