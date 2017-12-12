INSERT INTO hpo SELECT *
                FROM CSVREAD('${import.path}/hpo.pg', 'lcname|id|prefname',
                             'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');
