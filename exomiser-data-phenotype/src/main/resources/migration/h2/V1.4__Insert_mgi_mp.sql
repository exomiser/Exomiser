INSERT INTO mgi_mp SELECT * FROM CSVREAD('${import.path}/mouseMp.pg', 'mgi_gene_id|mgi_gene_symbol|mouse_model_id|mp_id','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

