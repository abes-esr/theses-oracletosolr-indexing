create or replace TRIGGER "STAR"."INDEXATION_SOLR_TRIGGER" after INSERT or UPDATE
on DOCUMENT
FOR EACH ROW

DECLARE

iddocparam number;
docparam xmltype;

BEGIN

if not globals_pkg.gv_run_my_trigger
    then return;
end if;

iddocparam := :new.iddoc;
--DBMS_OUTPUT.PUT_LINE ( 'iddocparam : '|| iddocparam);

if :new.doc is not null
    then docparam := :new.doc;
    indexationsolr(iddocparam,docparam);
end if;

END;