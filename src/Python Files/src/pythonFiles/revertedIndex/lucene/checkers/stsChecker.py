def main():
    # docLength
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\out\df.sts'
    f = open(path,'r')
    sum = 0
    f.readline()
    for line in f:
        parts = line.split()
        num = int(parts[1])
        sum += num
    print('Fineal Sum =',sum)
if __name__ == '__main__':
    main()