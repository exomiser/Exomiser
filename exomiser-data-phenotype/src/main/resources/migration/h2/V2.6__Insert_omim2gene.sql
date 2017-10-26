INSERT INTO omim2gene SELECT * FROM CSVREAD('${import.path}/omim2gene.pg', 'mimdiseaseid|mimdiseasename|cytoband|mimgeneid|entrezgeneid|genesymbol|seriesid','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

