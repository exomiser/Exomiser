INSERT INTO omim SELECT * FROM CSVREAD('${import.path}/omim.pg', 'phenmim|genemim|diseasename|gene_id|type|inheritance','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');
