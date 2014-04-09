INSERT INTO hp_mp_mappings SELECT * FROM CSVREAD('${import.path}/hpMpMapping.pg', 'mapping_id|hp_id|mp_id|score','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

