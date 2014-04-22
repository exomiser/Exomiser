INSERT INTO hp_zp_mappings SELECT * FROM CSVREAD('${import.path}/hpZpMapping.pg', 'mapping_id|hp_id|zp_id|score','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

