--------------------------------------------------------------------------------
-- if the project is in java 1.8 with tomcat8
--------------------------------------------------------------------------------
create or replace TRIGGER "SUJETS"."INDEXATION_SOLR_TRIGGER" after INSERT or UPDATE
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

--------------------------------------------------------------------------------
-- if the project is in java 11 with tomcat9
--------------------------------------------------------------------------------

create or replace TRIGGER "SUJETS"."INDEXATION_SOLR_TRIGGER" after INSERT or UPDATE
on DOCUMENT
FOR EACH ROW

DECLARE

iddocparam number;

BEGIN

if not globals_pkg.gv_run_my_trigger
    then return;
end if;

iddocparam := :new.iddoc;
--DBMS_OUTPUT.PUT_LINE ( 'iddocparam : '|| iddocparam);

if :new.doc is not null
    then
    indexationsolr(iddocparam);
end if;

END;