INSERT INTO frequency SELECT * FROM CSVREAD('${import.path}/frequency.pg', 'chromosome|position|ref|alt|rsid|dbsnpmaf|espmeamaf|espaamaf|espallmaf|exacafrmaf|exacamrmaf|exaceasmaf|exacfinmaf|exacnfemaf|exacothmaf|exacsasmaf','charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

