INSERT INTO phenoseries SELECT * FROM CSVREAD('${import.path}/phenoseries.pg', 'seriesid|name','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

