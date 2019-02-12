INSERT INTO hpo SELECT *
                FROM CSVREAD('${import.path}/hpo.pg', 'id|lcname',
                             'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');
