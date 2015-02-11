INSERT INTO metadata SELECT * FROM CSVREAD('${import.path}/metadata.pg', 'resource|version','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

