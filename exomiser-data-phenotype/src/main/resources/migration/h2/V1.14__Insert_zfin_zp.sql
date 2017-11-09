INSERT INTO zfin_zp SELECT * FROM CSVREAD('${import.path}/fishZp.pg', 'zfin_gene_id|zfin_gene_symbol|zfin_model_id|zp_id','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

