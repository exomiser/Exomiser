INSERT INTO zp SELECT * FROM CSVREAD('${import.path}/zp.pg', 'zp_id|zp_term','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');
