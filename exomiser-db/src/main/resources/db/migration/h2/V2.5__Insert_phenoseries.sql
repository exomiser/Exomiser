INSERT INTO phenoseries SELECT * FROM CSVREAD('${import.path}/phenoseries.pg', 'seriesid|name|genecount','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

