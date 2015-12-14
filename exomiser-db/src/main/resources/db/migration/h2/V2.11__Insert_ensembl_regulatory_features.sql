INSERT INTO regulatory_regions SELECT * FROM CSVREAD('${import.path}/ensembl_enhancers.pg', 'chromosome|start|end|feature_type','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

