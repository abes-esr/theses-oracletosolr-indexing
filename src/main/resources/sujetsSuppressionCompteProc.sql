create or replace PROCEDURE suppressioncompte (idoc NUMBER) AS
BEGIN
 DELETE FROM COMPTE WHERE IDDOC=idoc;
END suppressioncompte;