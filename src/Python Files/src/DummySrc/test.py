import pandas as pd

if __name__ == '__main__':
    s = pd.Series([True , False , False])
    s =  ~s
    for idx , val in s.items():
        print(idx,val)
    print('Done')
