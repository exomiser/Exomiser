INSERT INTO clinvar SELECT * FROM CSVREAD('${import.path}/clinvar.pg', 'chromosome|position|id|signif','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');
