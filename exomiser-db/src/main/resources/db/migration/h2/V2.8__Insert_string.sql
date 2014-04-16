INSERT INTO string SELECT * FROM CSVREAD('${import.path}/string.pg', 'entrezA|entrezB|score','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

