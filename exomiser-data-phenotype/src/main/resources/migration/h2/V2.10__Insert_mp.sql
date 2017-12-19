INSERT INTO mp SELECT *
               FROM CSVREAD('${import.path}/mp.pg', 'mp_id|mp_term',
                            'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');
