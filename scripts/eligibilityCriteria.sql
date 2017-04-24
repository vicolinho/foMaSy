Drop table if exists ec_DILS;

Create table ec_DILS (
 accession varchar(100) ,
 question varchar(4000) ,
 Primary Key (accession)
);

 LOAD DATA LOCAL INFILE 'C:/Users/christen/intelijWorkspace/fomasy/testFiles/eligibilityCriteria.txt'
   Into Table ec_DILS
   CHARSET utf8 
   Fields Terminated BY '\t' 
   Enclosed by '"'
	Lines Terminated BY '\n' 
   Ignore 1 Lines 
  (accession, question);
  
  
  Drop table if exists ec_Julian;

Create table ec_Julian (
 question varchar(4000) ,

);

 LOAD DATA LOCAL INFILE 'C:/Users/christen/intelijWorkspace/fomasy/testFiles/criteriaJulian.txt'
   Into Table ec_Julian
   CHARSET utf8 
   Fields Terminated BY '\t' 
   Enclosed by '"'
   Lines Terminated BY '\n' 
   Ignore 1 Lines 
  (question);