create or replace TRIGGER "PORTAIL"."INDEXATION_SOLR_TRIGGER" after INSERT or UPDATE
on DOCUMENT
FOR EACH ROW

DECLARE

iddocparam number;
docparam xmltype;
texteparam clob;
dateparam varchar2(50);

BEGIN

if not globals_pkg.gv_run_my_trigger
    then return;
end if;

iddocparam := :new.iddoc;
DBMS_OUTPUT.PUT_LINE ( 'iddocparam : '|| iddocparam);
texteparam := :new.texte;
dateparam := TO_CHAR(:new.dateinsertion, 'YYYY-MM-DD')||'T'||TO_CHAR(:new.dateinsertion, 'hh24:mi:ss')||'Z';


if :new.doc is not null
    then docparam := :new.doc;
    indexationsolr(iddocparam,docparam,texteparam,dateparam);
end if;

END;