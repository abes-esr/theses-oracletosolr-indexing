-------------------------------------------------------------------------
-- the proc puts logs inside the PROCESSING_LOG table
-- so beforehand, it's necessary to create the table

CREATE TABLE PROCESSING_LOG
   (	"MESSAGE_DATE" TIMESTAMP (6),
	"MESSAGE_TEXT" VARCHAR2(4000 BYTE),
	"MESSAGE_CLOB" CLOB
   );



create or replace PROCEDURE "SP_LOG" (
    P_MESSAGE_TEXT VARCHAR2,P_MESSAGE_CLOB CLOB
) IS
  pragma autonomous_transaction;
BEGIN

    DBMS_OUTPUT.PUT_LINE(P_MESSAGE_TEXT);

    INSERT INTO PROCESSING_LOG (
        MESSAGE_DATE,
        MESSAGE_TEXT,
        MESSAGE_CLOB
    ) VALUES (
        SYSDATE,
        P_MESSAGE_TEXT,
        P_MESSAGE_CLOB
    );
    COMMIT;

END;