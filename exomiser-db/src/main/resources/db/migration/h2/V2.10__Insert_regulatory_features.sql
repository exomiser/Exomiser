INSERT INTO regulatory_features SELECT * FROM CSVREAD('${import.path}/regulatory_features.pg', 'chromosome|start|end|feature_type|tissue','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

