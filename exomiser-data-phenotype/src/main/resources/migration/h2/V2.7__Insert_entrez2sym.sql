INSERT INTO entrez2sym SELECT *
                       FROM CSVREAD('${import.path}/entrez2sym.pg', 'entrezID|symbol',
                                    'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

