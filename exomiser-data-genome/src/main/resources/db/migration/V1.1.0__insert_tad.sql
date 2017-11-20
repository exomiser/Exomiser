INSERT INTO tad SELECT *
                FROM CSVREAD('${import.path}/tad.pg', 'chromosome|start|end|entrezid|symbol',
                             'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');
