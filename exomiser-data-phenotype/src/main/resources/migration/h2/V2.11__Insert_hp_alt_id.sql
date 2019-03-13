INSERT INTO hp_alt_ids SELECT *
FROM CSVREAD('${import.path}/hp_alt_ids.pg', 'alt_id|primary_id',
             'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');