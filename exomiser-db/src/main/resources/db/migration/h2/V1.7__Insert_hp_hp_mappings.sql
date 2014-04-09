INSERT INTO hp_hp_mappings SELECT * FROM CSVREAD('${import.path}/hpHpmapping.pg', 'mapping_id|hp_id|hp_id_hit|score','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=null');

